package com.example.learningApp.dto.response.order;

import com.example.learningApp.enums.ProductType;
import lombok.Data;

@Data
public class OrderItemResponse {

    private String id;
    private ProductType productType;
    private String courseId;
    private String courseName;
    private String vipPackageId;
    private String vipPackageName;
    private Long price;
}