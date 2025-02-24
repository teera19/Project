package com.example.server_management;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootTest
class ServerManagementApplicationTests {
	public static void main(String[] args) {
		SpringApplication.run(ServerManagementApplication.class, args);
	}
	@Test
	void contextLoads() {
	}

}
