package com.example.learningApp.dto.request.payment;

import com.example.learningApp.enums.ProductType;
import lombok.Data;

@Data
public class CreateVnPayRequest {
    private String productId;   // vipPackageId hoặc courseId
    private ProductType productType; // VIP_PACKAGE hoặc COURSE}
}
