package com.example.learningApp.repository;

import com.example.learningApp.entity.VipPackage;
import com.example.learningApp.enums.PlanType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VipPackageRepository extends JpaRepository<VipPackage, String> {

    boolean existsByPlanType(PlanType planType);

    List<VipPackage> findByActiveTrue();
}
