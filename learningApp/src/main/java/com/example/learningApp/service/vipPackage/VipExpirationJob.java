package com.example.learningApp.service.vipPackage;

import com.example.learningApp.entity.User;
import com.example.learningApp.entity.VipSubscription;
import com.example.learningApp.repository.UserRepository;
import com.example.learningApp.repository.VipSubscriptionRepository;
import com.example.learningApp.service.role.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
@Component
@RequiredArgsConstructor
@Slf4j
public class VipExpirationJob {

    private final VipSubscriptionRepository vipSubscriptionRepository;
    private final UserRepository userRepository;
    private final RoleService roleService;

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void downgradeExpiredVipUsers() {

        LocalDateTime now = LocalDateTime.now();

        // 1️⃣ Lấy subscription đã hết hạn nhưng còn active
        List<VipSubscription> expiredSubs =
                vipSubscriptionRepository
                        .findByExpiredDateBeforeAndActiveTrue(now);

        if (expiredSubs.isEmpty()) {
            log.info("✅ No expired VIP subscriptions");
            return;
        }

        for (VipSubscription sub : expiredSubs) {

            User user = sub.getUser();

            // 2️⃣ deactivate subscription
            sub.setActive(false);

            // 3️⃣ Kiểm tra user còn subscription active nào không
            boolean stillHasActiveVip =
                    vipSubscriptionRepository
                            .existsByUserIdAndActiveTrue(user.getId());

            if (!stillHasActiveVip) {

                // downgrade role
                roleService.changeUserRole(
                        user.getId(),
                        "USER_VIP",
                        "USER"
                );

                user.setVipExpiredAt(null);
                userRepository.save(user);

                log.info("⬇ Downgraded user {} to USER",
                        user.getEmail());
            }
        }
    }
}
