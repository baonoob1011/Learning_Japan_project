package com.example.learningApp.service.payment;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.dto.response.order.OrderSuccessResponse;
import com.example.learningApp.entity.Order;
import com.example.learningApp.entity.OrderItem;
import com.example.learningApp.enums.PaymentStatus;
import com.example.learningApp.enums.ProductType;
import com.example.learningApp.service.order.OrderService;
import com.example.learningApp.exception.PaymentExpiredException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class VnPayService {

    private final OrderService orderService;
    private final EntityFinder finder;
    private final PaymentRetryRedisService retryRedisService;

    @Transactional
    public Map<String, Object> createPaymentRequest(String productId, ProductType productType) throws Exception {
        var user = finder.userById();

        boolean locked = retryRedisService.acquireCreateLock(user.getId(), productType.name(), productId);
        if (!locked) {
            throw new RuntimeException("Ban thao tac qua nhanh. Vui long thu lai sau vai giay.");
        }

        Long amount;
        String orderInfo;

        if (productType == ProductType.VIP_PACKAGE) {
            var vipPackage = finder.vipPackageById(productId);
            amount = vipPackage.getPrice();
            orderInfo = "Thanh toan goi VIP " + vipPackage.getName();
        } else if (productType == ProductType.COURSE) {
            var course = finder.courseById(productId);
            amount = course.getPrice();
            orderInfo = "Thanh toan khoa hoc " + course.getTitle();
        } else {
            throw new RuntimeException("Invalid product type");
        }

        String txnRef = user.getId() + "_" + UUID.randomUUID();
        Order order = orderService.createPendingOrder(productId, productType, txnRef, amount);

        retryRedisService.initRetryWindow(order.getId(), user.getId(), txnRef);
        String paymentUrl = buildPaymentUrl(amount, orderInfo, txnRef);

        return Map.of(
                "paymentUrl", paymentUrl,
                "orderId", order.getId(),
                "orderCode", order.getOrderCode(),
                "gateway", "VNPAY");
    }

    @Transactional
    public Map<String, Object> retryPaymentRequest(String currentOrderCode) throws Exception {
        var currentUser = finder.userById();
        Order order = orderService.findMyOrderByCode(currentOrderCode);

        if (order.getStatus() == PaymentStatus.SUCCESS) {
            throw new RuntimeException("Hoa don da thanh toan thanh cong, khong the retry.");
        }

        if (!retryRedisService.canRetry(order.getId(), currentUser.getId(), currentOrderCode)) {
            if (retryRedisService.isRetryExpired(order.getId())) {
                orderService.markOrderExpired(order.getId());
                retryRedisService.markRetryExpired(order.getId());
                throw new PaymentExpiredException("Đã qua 15 phút retry, hóa đơn đã hết hạn.");
            }
            throw new RuntimeException("Khong the retry hoa don nay.");
        }

        OrderItem item = order.getOrderItems().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Order item not found"));

        String newTxnRef = currentUser.getId() + "_" + UUID.randomUUID();
        orderService.rotateOrderCode(order.getId(), newTxnRef);
        retryRedisService.rotateTxnRef(order.getId(), newTxnRef);

        String orderInfo = resolveOrderInfo(item);
        String paymentUrl = buildPaymentUrl(order.getAmount(), orderInfo, newTxnRef);

        return Map.of(
                "paymentUrl", paymentUrl,
                "orderId", order.getId(),
                "orderCode", newTxnRef,
                "gateway", "VNPAY",
                "retry", true);
    }

    @Transactional
    public OrderSuccessResponse handleVnPayReturn(Map<String, String> vnpResponse) {
        log.info("VNPAY RETURN: {}", vnpResponse);

        String txnRef = vnpResponse.get("vnp_TxnRef");
        String responseCode = vnpResponse.get("vnp_ResponseCode");
        String transactionNo = vnpResponse.get("vnp_TransactionNo");
        String receivedSecureHash = vnpResponse.get("vnp_SecureHash");
        Order order = findOrFailOrder(txnRef).orElseThrow(() -> new RuntimeException("Order not found"));

        if (!retryRedisService.acquireCallbackLock(txnRef, transactionNo)) {
            if (order.getStatus() == PaymentStatus.SUCCESS) {
                return orderService.markOrderSuccess(order.getOrderCode(), order.getTransactionNo());
            }
            throw new RuntimeException("Callback is being processed");
        }

        if (retryRedisService.isCallbackProcessed(txnRef, transactionNo)) {
            return orderService.markOrderSuccess(order.getOrderCode(), order.getTransactionNo());
        }

        if (!validateSecureHash(vnpResponse, receivedSecureHash)) {
            log.error("Secure hash validation failed for txnRef: {}", txnRef);
            orderService.markOrderFailedById(order.getId());
            throw new RuntimeException("Invalid secure hash");
        }

        if (!"00".equals(responseCode)) {
            orderService.markOrderFailedById(order.getId());
            throw new RuntimeException("Payment failed");
        }

        OrderSuccessResponse response = orderService.markOrderSuccess(order.getOrderCode(), transactionNo);
        retryRedisService.markCallbackProcessed(txnRef, transactionNo);
        retryRedisService.markRetryExpired(order.getId());
        return response;
    }

    @Transactional
    public void handleVnPayIpn(Map<String, String> vnpResponse) {
        String txnRef = vnpResponse.get("vnp_TxnRef");
        String responseCode = vnpResponse.get("vnp_ResponseCode");
        String transactionNo = vnpResponse.get("vnp_TransactionNo");
        String receivedSecureHash = vnpResponse.get("vnp_SecureHash");
        Order order = findOrFailOrder(txnRef).orElseThrow(() -> new RuntimeException("Order not found"));

        if (!retryRedisService.acquireCallbackLock(txnRef, transactionNo)) {
            return;
        }

        if (retryRedisService.isCallbackProcessed(txnRef, transactionNo)) {
            return;
        }

        if (!validateSecureHash(vnpResponse, receivedSecureHash)) {
            throw new RuntimeException("Invalid checksum");
        }

        if ("00".equals(responseCode)) {
            orderService.markOrderSuccess(order.getOrderCode(), transactionNo);
            retryRedisService.markCallbackProcessed(txnRef, transactionNo);
            retryRedisService.markRetryExpired(order.getId());
        } else {
            orderService.markOrderFailedById(order.getId());
        }
    }

    private String buildPaymentUrl(Long amount, String orderInfo, String txnRef) throws Exception {
        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnp_TmnCode);
        vnpParams.put("vnp_Amount", String.valueOf(amount * 100));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", txnRef);
        vnpParams.put("vnp_OrderInfo", orderInfo);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnpParams.put("vnp_IpAddr", "1.1.1.1");
        vnpParams.put("vnp_CreateDate", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));

        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (Iterator<String> itr = fieldNames.iterator(); itr.hasNext();) {
            String fieldName = itr.next();
            String fieldValue = vnpParams.get(fieldName);

            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName)
                        .append("=")
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                        .append("=")
                        .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                if (itr.hasNext()) {
                    hashData.append("&");
                    query.append("&");
                }
            }
        }

        String secureHash = hmacSHA512(vnp_HashSecret, hashData.toString());
        return vnp_PayUrl + "?" + query + "&vnp_SecureHash=" + secureHash;
    }

    private String resolveOrderInfo(OrderItem item) {
        if (item.getProductType() == ProductType.VIP_PACKAGE && item.getVipPackage() != null) {
            return "Thanh toan goi VIP " + item.getVipPackage().getName();
        }
        if (item.getProductType() == ProductType.COURSE && item.getCourse() != null) {
            return "Thanh toan khoa hoc " + item.getCourse().getTitle();
        }
        return "Thanh toan don hang";
    }

    private Optional<Order> findOrFailOrder(String txnRef) {
        Optional<Order> byCode = orderService.findByOrderCode(txnRef);
        if (byCode.isPresent()) {
            return byCode;
        }
        return retryRedisService.resolveOrderIdByTxnRef(txnRef).flatMap(orderService::findById);
    }

    private boolean validateSecureHash(Map<String, String> vnpResponse, String receivedSecureHash) {
        try {
            Map<String, String> data = new HashMap<>(vnpResponse);
            data.remove("vnp_SecureHash");
            data.remove("vnp_SecureHashType");

            List<String> fieldNames = new ArrayList<>(data.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();
            for (Iterator<String> itr = fieldNames.iterator(); itr.hasNext();) {
                String fieldName = itr.next();
                String fieldValue = data.get(fieldName);

                if (fieldValue != null && !fieldValue.isEmpty()) {
                    hashData.append(fieldName)
                            .append("=")
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                    if (itr.hasNext()) {
                        hashData.append("&");
                    }
                }
            }

            String calculatedSecureHash = hmacSHA512(vnp_HashSecret, hashData.toString());
            return calculatedSecureHash.equalsIgnoreCase(receivedSecureHash);
        } catch (Exception e) {
            log.error("Error validating secure hash: {}", e.getMessage());
            return false;
        }
    }

    private String hmacSHA512(String key, String data) throws Exception {
        Mac hmac512 = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        hmac512.init(secretKeySpec);

        byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hash = new StringBuilder();
        for (byte b : bytes) {
            hash.append(String.format("%02x", b));
        }
        return hash.toString();
    }
}
