package com.example.server_management.rest_controllers;

import com.example.server_management.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class Logout {
    @Autowired
    private UserRepository userRepository;
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpSession session) {
        // ลบข้อมูลทั้งหมดในเซสชัน
        session.invalidate();

        // ส่งข้อความตอบกลับเพื่อแจ้งว่า logout สำเร็จ
        return new ResponseEntity<>("Logout successful.", HttpStatus.OK);
    }
}
