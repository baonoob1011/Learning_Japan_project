package com.example.learningApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import jakarta.annotation.PostConstruct;
import java.util.TimeZone;

@EnableCaching
@EnableScheduling
@SpringBootApplication
public class LearningAppApplication {

	@PostConstruct
	public void init() {
		// ✅ Thiết lập timezone hệ thống theo Việt Nam (UTC+7)
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
	}

	public static void main(String[] args) {
		SpringApplication.run(LearningAppApplication.class, args);
	}

}
