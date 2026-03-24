package com.example.learningApp.mapper;

import com.example.learningApp.dto.response.order.OrderDetailResponse;
import com.example.learningApp.dto.response.order.OrderItemResponse;
import com.example.learningApp.dto.response.order.OrderResponse;
import com.example.learningApp.dto.response.order.OrderSuccessResponse;
import com.example.learningApp.entity.Order;
import com.example.learningApp.entity.OrderItem;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    /* ================= ORDER RESPONSE ================= */

    OrderResponse toOrderResponse(Order order);

    /* ================= ORDER DETAIL ================= */

    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "items", source = "orderItems")
    OrderDetailResponse toOrderDetailResponse(Order order);

    /* ================= ORDER SUCCESS ================= */

    @Mapping(target = "orderId", source = "id")
    OrderSuccessResponse toOrderSuccessResponse(Order order);

    /* ================= ORDER ITEM ================= */

    @Mapping(target = "id", source = "id")
    @Mapping(target = "productType", source = "productType")
    @Mapping(target = "price", source = "price")

    @Mapping(target = "courseId", expression = "java(getCourseId(item))")
    @Mapping(target = "courseName", expression = "java(getCourseName(item))")

    @Mapping(target = "vipPackageId", expression = "java(getVipId(item))")
    @Mapping(target = "vipPackageName", expression = "java(getVipName(item))")
    @Mapping(target = "courseTitle", ignore = true)
    OrderItemResponse toOrderItemResponse(OrderItem item);

    /* ================= HELPER METHODS ================= */

    default String getCourseId(OrderItem item) {
        return item.getCourse() != null ? item.getCourse().getId() : null;
    }

    default String getCourseName(OrderItem item) {
        return item.getCourse() != null ? item.getCourse().getTitle() : null;
    }

    default String getVipId(OrderItem item) {
        return item.getVipPackage() != null ? item.getVipPackage().getId() : null;
    }

    default String getVipName(OrderItem item) {
        return item.getVipPackage() != null ? item.getVipPackage().getName() : null;
    }
}
