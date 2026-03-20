package com.example.learningApp.configuration;

import com.example.learningApp.service.init.AdminInitializationService;
import com.example.learningApp.service.init.RoleInitializationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SystemStartupRunner implements ApplicationRunner {

    private final RoleInitializationService roleInitService;
    private final AdminInitializationService adminInitService;

    @Override
    public void run(ApplicationArguments args) {

        log.info("🚀 System startup initialization");

        roleInitService.initDefaultRoles();
        adminInitService.initAdminUser();

        log.info("✅ System startup completed");
    }
}

