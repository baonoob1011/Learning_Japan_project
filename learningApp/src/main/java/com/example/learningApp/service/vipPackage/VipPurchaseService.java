package com.example.learningApp.service.vipPackage;

import com.example.learningApp.common.EntityFinder;
import com.example.learningApp.entity.Role;
import com.example.learningApp.entity.User;
import com.example.learningApp.repository.RoleRepository;
import com.example.learningApp.repository.UserRepository;
import com.example.learningApp.service.role.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
@Slf4j
public class VipPurchaseService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
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

        log.info("🚀 Processing VIP upgrade for user: {}", user.getEmail());

        // ===== 4️⃣ Ensure roles exist and Change role USER -> USER_VIP =====
        roleService.createRoleIfNotExists("USER");
        roleService.createRoleIfNotExists("USER_VIP");

        roleService.changeUserRole(user.getId(), "USER", "USER_VIP");

        // Ensure DB role is VIP even if external provider update is partially failed.
        User persistedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found after VIP purchase"));

        Role vipRole = roleRepository.findByRoleName("USER_VIP")
                .orElseThrow(() -> new RuntimeException("Role USER_VIP not found"));

        if (persistedUser.getRoles() == null) {
            persistedUser.setRoles(new HashSet<>());
        }

        persistedUser.getRoles().removeIf(r -> "USER".equalsIgnoreCase(r.getRoleName()));
        if (persistedUser.getRoles().stream().noneMatch(r -> "USER_VIP".equalsIgnoreCase(r.getRoleName()))) {
            persistedUser.getRoles().add(vipRole);
        }

        userRepository.save(persistedUser);

        log.info("🔥 User {} upgraded to VIP successfully", user.getEmail());
    }
}
