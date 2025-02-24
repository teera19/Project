package com.example.server_management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling  // ✅ ต้องใส่ที่นี่
@SpringBootApplication
public class ServerManagementApplication {
	public static void main(String[] args) {
		SpringApplication.run(ServerManagementApplication.class, args);
	}
}
