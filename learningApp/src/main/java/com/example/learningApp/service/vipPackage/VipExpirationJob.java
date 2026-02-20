package com.example.learningApp.service.vipPackage;

import com.example.learningApp.entity.User;
import com.example.learningApp.repository.UserRepository;
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

    private final UserRepository userRepository;
    private final RoleService roleService;

    // Chạy mỗi ngày lúc 02:00 sáng
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void downgradeExpiredVipUsers() {

        LocalDateTime now = LocalDateTime.now();

        List<User> expiredUsers =
                userRepository.findByVipExpiredAtBefore(now);

        if (expiredUsers.isEmpty()) {
            log.info("✅ No expired VIP users found");
            return;
        }

        for (User user : expiredUsers) {

            try {

                roleService.changeUserRole(
                        user.getId(),
                        "USER_VIP",
                        "USER"
                );

                user.setVipExpiredAt(null);
                userRepository.save(user);

                log.info("⬇ Downgraded user {} to USER",
                        user.getEmail());

            } catch (Exception e) {
                log.error("❌ Failed to downgrade user {}",
                        user.getEmail(), e);
            }
        }
    }
}
