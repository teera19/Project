package com.example.server_management.rest_controllers;

import com.example.server_management.dto.UserProfile;
import com.example.server_management.models.User;
import com.example.server_management.repository.UserRepository;
import com.example.server_management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/")
public class MyProfile {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/my-profile")
    public ResponseEntity<?> getUserProfile(HttpSession session) {
        // ดึงข้อมูลจาก session เพื่อหาผู้ใช้ที่ล็อกอิน
        String userName = (String) session.getAttribute("user_name");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=UTF-8");

        if (userName != null) {
            // ค้นหาผู้ใช้ในฐานข้อมูล
            User existingUser = userRepository.findByUserName(userName);

            if (existingUser != null) {
                // ส่งเฉพาะข้อมูล name และ lastname
                UserProfile userProfile = new UserProfile(existingUser.getName(), existingUser.getLastName(),existingUser.getEmail());
                return new ResponseEntity<>(userProfile, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("User not found.", HttpStatus.NOT_FOUND);
            }
        } else {
            return new ResponseEntity<>("User is not logged in.", HttpStatus.UNAUTHORIZED);
        }
    }

}

