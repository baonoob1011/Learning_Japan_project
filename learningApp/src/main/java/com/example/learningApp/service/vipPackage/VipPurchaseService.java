package com.example.learningApp.service.vipPackage;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.entity.User;
import com.example.learningApp.entity.VipPackage;
import com.example.learningApp.repository.UserRepository;
import com.example.learningApp.repository.VipPackageRepository;
import com.example.learningApp.service.role.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class VipPurchaseService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final EntityFinder entityFinder;

    @Transactional
    public void purchaseVip(String vipPackageId, String userId) {

        var user = entityFinder.userId(userId);
        var vipPackage = entityFinder.vipPackageById(vipPackageId);

        if (!Boolean.TRUE.equals(vipPackage.getActive())) {
            throw new RuntimeException("Vip package is not active");
        }

        // ===== Check VIP status =====
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentExpire = user.getVipExpiredAt();

        if (currentExpire != null && currentExpire.isAfter(now)) {
            throw new RuntimeException("Bạn đang là hội viên VIP và vẫn còn hạn sử dụng (đến " + currentExpire
                    + "). Không thể mua thêm lúc này.");
        }

        // ===== 3️⃣ Tính expire date =====
        if (vipPackage.getDurationDays() == null) {
            // LIFETIME
            user.setVipExpiredAt(null);
        } else {
            user.setVipExpiredAt(now.plusDays(vipPackage.getDurationDays()));
        }

        userRepository.save(user);

        // ===== 4️⃣ Change role USER -> USER_VIP =====
        roleService.changeUserRole(
                user.getId(),
                "USER",
                "USER_VIP");

        log.info("🔥 User {} upgraded to VIP", user.getEmail());
    }
}
