package com.example.learningApp.controller.vipPackage;

import com.example.learningApp.common.ApiResponse;
import com.example.learningApp.dto.request.vipPackage.CreateVipPackageRequest;
import com.example.learningApp.dto.request.vipPackage.UpdateVipPackageRequest;
import com.example.learningApp.dto.response.vipPackage.VipPackageResponse;
import com.example.learningApp.service.vipPackage.VipPackageService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vip-packages")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class VipPackageController {

    VipPackageService vipPackageService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createVipPackage(
            @RequestBody @Valid CreateVipPackageRequest request) {

        vipPackageService.createVipPackage(request);
        return ResponseEntity.ok(
                ApiResponse.success("VIP package created successfully", null)
        );
    }
    @GetMapping
    public ResponseEntity<ApiResponse<List<VipPackageResponse>>> getAllVipPackages() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Get VIP packages successfully",
                        vipPackageService.getAllActivePackages()
                )
        );
    }
    // UPDATE VIP PACKAGE
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateVipPackage(
            @PathVariable String id,
            @RequestBody @Valid UpdateVipPackageRequest request) {

        vipPackageService.updateVipPackage(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("VIP package updated successfully", null)
        );
    }

    // DELETE VIP PACKAGE (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVipPackage(
            @PathVariable String id) {

        vipPackageService.deleteVipPackage(id);

        return ResponseEntity.ok(
                ApiResponse.success("VIP package deleted successfully", null)
        );
    }

}
