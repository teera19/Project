package com.example.server_management.rest_controllers;


import com.example.server_management.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/")
public class Checkshop {

    @Autowired
    private UserService userService; // ใช้ UserService แทนการเรียก Repository โดยตรง

    @GetMapping("/checkshop")
    public ResponseEntity<?> checkShop(HttpSession session) {
        // ดึง user_name จาก HttpSession
        String userName = (String) session.getAttribute("user_name");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=UTF-8");

        // เช็คว่า session มี user_name หรือไม่
        if (userName == null) {
            return new ResponseEntity<>(Map.of(
                    "message", "User not logged in. Please log in first."
            ), HttpStatus.UNAUTHORIZED);
        }

        // ใช้ userService เพื่อเช็คว่าผู้ใช้มีร้านค้าหรือไม่
        String message = userService.userHasShop(userName);

        // กำหนดสถานะ HTTP ตามผลลัพธ์
        HttpStatus status = message.equals("User not found") ? HttpStatus.NOT_FOUND : HttpStatus.OK;

        return new ResponseEntity<>(Map.of(
                "message", message
        ), status);
    }
}


