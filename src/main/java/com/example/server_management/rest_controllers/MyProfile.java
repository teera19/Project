package com.example.server_management.rest_controllers;

import com.example.server_management.models.User;
import com.example.server_management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/")
public class MyProfile {

    @Autowired
    private UserService userService;

    @GetMapping("/my-profile")
    public ResponseEntity<Object> getMyProfile(HttpSession session) {
        // ดึง userName จาก Session
        String userName = (String) session.getAttribute("user_name");

        if (userName == null || userName.isEmpty()) {
            return new ResponseEntity<>("User not logged in", HttpStatus.UNAUTHORIZED);
        }

        // ค้นหาผู้ใช้ในฐานข้อมูล
        User user = userService.getUserByUserName(userName);

        if (user == null) {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }

        // จัดเรียงลำดับฟิลด์ใน Map ตามต้องการ
        Map<String, Object> response = Map.of(
                "email", user.getEmail(),
                "name", user.getName(),
                "lastName", user.getLastName()



        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}

