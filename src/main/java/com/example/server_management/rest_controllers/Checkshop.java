package com.example.server_management.rest_controllers;


import com.example.server_management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/")
public class Checkshop {
    @Autowired
    private UserService userService;
    @GetMapping("/checkshop")
    public ResponseEntity<Map<String, Object>> checkShop(@RequestParam("user_name") String userName) {
        String message = userService.userHasShop(userName);

        // เช็คข้อความที่ได้จาก Service เพื่อกำหนด HTTP Status
        HttpStatus status = message.equals("User not found") ? HttpStatus.NOT_FOUND : HttpStatus.OK;

        return new ResponseEntity<>(Map.of(
                "message", message
        ), status);
    }
}
