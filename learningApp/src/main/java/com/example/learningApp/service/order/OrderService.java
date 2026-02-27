package com.example.learningApp.service.order;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.dto.response.order.OrderDetailResponse;
import com.example.learningApp.dto.response.order.OrderSuccessResponse;
import com.example.learningApp.entity.Enrollment;
import com.example.learningApp.entity.Order;
import com.example.learningApp.entity.OrderItem;
import com.example.learningApp.entity.User;
import com.example.learningApp.entity.VipPackage;
import com.example.learningApp.entity.VipSubscription;
import com.example.learningApp.enums.PaymentStatus;
import com.example.learningApp.enums.ProductType;
import com.example.learningApp.mapper.OrderMapper;
import com.example.learningApp.repository.EnrollmentRepository;
import com.example.learningApp.repository.OrderRepository;
import com.example.learningApp.repository.UserRepository;
import com.example.learningApp.repository.VipPackageRepository;
import com.example.learningApp.repository.VipSubscriptionRepository;
import com.example.learningApp.service.vipPackage.VipPurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class OrderService {

        private final OrderRepository orderRepository;
        private final VipPurchaseService vipPurchaseService;
        private final EnrollmentRepository enrollmentRepository;
        private final VipSubscriptionRepository vipSubscriptionRepository;
        private final OrderMapper orderMapper;
        private final EntityFinder finder;


        public List<OrderDetailResponse> getMyOrders() {

                var currentUser = finder.userById();

                return orderRepository
                        .findByUserIdOrderByCreatedAtDesc(currentUser.getId())
                        .stream()
                        .map(orderMapper::toOrderDetailResponse)
                        .toList();
        }
        public OrderDetailResponse getOrderDetail(String orderCode) {

                var currentUser = finder.userById();

                Order order = orderRepository
                        .findByOrderCodeAndUserId(orderCode, currentUser.getId())
                        .orElseThrow(() -> new RuntimeException("Order not found"));

                return orderMapper.toOrderDetailResponse(order);
        }
        public Order createPendingOrder(
                String productId,
                ProductType productType,
                String orderCode,
                Long amount) {

                var user = finder.userById();

                Order order = Order.builder()
                        .user(user)
                        .orderCode(orderCode)
                        .amount(amount)
                        .paymentMethod("VNPAY")
                        .status(PaymentStatus.PENDING)
                        .createdAt(LocalDateTime.now())
                        .build();

                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .productType(productType)
                        .price(amount)
                        .build();

                if (productType == ProductType.VIP_PACKAGE) {

                        var vipPackage = finder.vipPackageById(productId);
                        orderItem.setVipPackage(vipPackage);

                } else if (productType == ProductType.COURSE) {

                        var course = finder.courseById(productId);

                        if (!course.getIsPaid()) {
                                throw new RuntimeException("This course is free");
                        }

                        boolean alreadyEnrolled =
                                enrollmentRepository.existsByUserIdAndCourseId(
                                        user.getId(), course.getId());

                        if (alreadyEnrolled) {
                                throw new RuntimeException("You already enrolled this course");
                        }

                        orderItem.setCourse(course);
                }

                order.setOrderItems(List.of(orderItem));

                return orderRepository.save(order);
        }
        /* ===================== SUCCESS ===================== */

        public OrderSuccessResponse markOrderSuccess(
                String orderCode,
                String transactionNo) {

                Order order = orderRepository.findByOrderCode(orderCode)
                        .orElseThrow(() -> new RuntimeException("Order not found"));

                // ✅ chống double success
                if (order.getStatus() == PaymentStatus.SUCCESS) {
                        return orderMapper.toOrderSuccessResponse(order);
                }

                order.setStatus(PaymentStatus.SUCCESS);
                order.setTransactionNo(transactionNo);
                order.setPaidAt(LocalDateTime.now());

                User user = order.getUser();

                for (OrderItem item : order.getOrderItems()) {

                        /* ================= COURSE ================= */
                        if (item.getProductType() == ProductType.COURSE) {

                                boolean alreadyEnrolled =
                                        enrollmentRepository.existsByUserIdAndCourseId(
                                                user.getId(),
                                                item.getCourse().getId());

                                if (!alreadyEnrolled) {
                                        Enrollment enrollment = Enrollment.builder()
                                                .user(user)
                                                .course(item.getCourse())
                                                .enrolledAt(LocalDateTime.now())
                                                .build();

                                        enrollmentRepository.save(enrollment);
                                }
                        }

                        /* ================= VIP ================= */
                        else if (item.getProductType() == ProductType.VIP_PACKAGE) {

                                // ✅ gọi service chuyên xử lý VIP
                                vipPurchaseService.purchaseVip(
                                        item.getVipPackage().getId(),
                                        user.getId()
                                );


                                // lưu subscription history
                                VipSubscription subscription = VipSubscription.builder()
                                        .user(user)
                                        .vipPackage(item.getVipPackage())
                                        .startDate(LocalDateTime.now())
                                        .expiredDate(user.getVipExpiredAt())
                                        .active(true)
                                        .build();

                                vipSubscriptionRepository.save(subscription);
                        }
                }

                orderRepository.save(order);

                return orderMapper.toOrderSuccessResponse(order);
        }

        /* ===================== FAILED ===================== */

        public void markOrderFailed(String orderCode) {

                Order order = orderRepository.findByOrderCode(orderCode)
                        .orElseThrow(() -> new RuntimeException("Order not found"));

                order.setStatus(PaymentStatus.FAILED);

                orderRepository.save(order);
        }
}
