package com.example.server_management.rest_controllers;

import com.example.server_management.models.User;
import com.example.server_management.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/")
public class Login {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User loginUser, HttpSession session) {
        try {
            // ค้นหาผู้ใช้จากฐานข้อมูล
            User existingUser = userRepository.findByUserName(loginUser.getUserName());

            // ตรวจสอบว่าผู้ใช้มีอยู่
            if (existingUser == null) {
                return new ResponseEntity<>(Map.of(
                        "message", "User not found."
                ), HttpStatus.NOT_FOUND);
            }

            // ตรวจสอบรหัสผ่าน
            if (!existingUser.getPassword().equals(loginUser.getPassword())) {
                return new ResponseEntity<>(Map.of(
                        "message", "Invalid password."
                ), HttpStatus.UNAUTHORIZED);
            }

            // เก็บข้อมูลลงใน Session
            session.setAttribute("user_name", existingUser.getUserName());

            // ลบรหัสผ่านก่อนส่งกลับ (เพื่อความปลอดภัย)
            existingUser.setPassword(null);

            // ส่งข้อมูลผู้ใช้กลับไป
            return new ResponseEntity<>(existingUser, HttpStatus.OK);

        } catch (Exception e) {
            // Log ข้อผิดพลาดเพื่อ Debug
            e.printStackTrace();
            return new ResponseEntity<>(Map.of(
                    "message", "An error occurred while processing the request."
            ), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}



