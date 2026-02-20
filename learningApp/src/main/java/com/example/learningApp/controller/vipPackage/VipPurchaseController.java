package com.example.learningApp.controller.vipPackage;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.service.vipPackage.VipPurchaseService;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vip-packages")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class VipPurchaseController {

    VipPurchaseService vipPurchaseService;

//    // USER mua VIP
//    @PostMapping("/purchase/{vipPackageId}")
//    public ResponseEntity<ApiResponse<Void>> purchaseVip(
//            @PathVariable @NotBlank String vipPackageId
//    ) {
//
//        vipPurchaseService.purchaseVip(vipPackageId);
//
//        return ResponseEntity.ok(
//                ApiResponse.success("VIP activated successfully", null)
//        );
//    }
}
