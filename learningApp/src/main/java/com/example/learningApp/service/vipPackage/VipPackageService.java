package com.example.learningApp.service.vipPackage;


import com.example.learningApp.dto.request.vipPackage.CreateVipPackageRequest;
import com.example.learningApp.dto.response.vipPackage.VipPackageResponse;
import com.example.learningApp.entity.VipPackage;
import com.example.learningApp.enums.PlanType;
import com.example.learningApp.repository.VipPackageRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class VipPackageService{

    VipPackageRepository vipPackageRepository;

    @Transactional
    public void createVipPackage(CreateVipPackageRequest request) {

        // Check trùng plan type (ví dụ chỉ cho 1 MONTHLY, 1 YEARLY, 1 LIFETIME)
        if (vipPackageRepository.existsByPlanType(request.getPlanType())) {
            throw new RuntimeException("Plan type already exists");
        }

        // Nếu là LIFETIME thì duration phải null
        if (request.getPlanType() == PlanType.LIFETIME) {
            request.setDurationDays(null);
        }

        VipPackage vipPackage = VipPackage.builder()
                .planType(request.getPlanType())
                .name(request.getName())
                .price(request.getPrice())
                .durationDays(request.getDurationDays())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        vipPackageRepository.save(vipPackage);
    }
    @Transactional(readOnly = true)
    public List<VipPackageResponse> getAllActivePackages() {

        return vipPackageRepository.findByActiveTrue()
                .stream()
                .map(vip -> VipPackageResponse.builder()
                        .id(vip.getId())
                        .planType(vip.getPlanType())
                        .name(vip.getName())
                        .price(vip.getPrice())
                        .durationDays(vip.getDurationDays())
                        .active(vip.getActive())
                        .build())
                .toList();
    }

}
