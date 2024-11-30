package com.example.server_management.rest_controllers;

import com.example.server_management.models.User;
import com.example.server_management.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class Login {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User loginUser, HttpSession session) {
        // ค้นหาผู้ใช้ในฐานข้อมูล
        User existingUser = userRepository.findByUserName(loginUser.getUserName());

        // ตรวจสอบความถูกต้องของรหัสผ่าน
        if (existingUser != null && loginUser.getPassword().equals(existingUser.getPassword())) {
            // ลบ password ออกก่อนที่จะส่งข้อมูลกลับไป
            existingUser.setPassword(null);  // ไม่ให้ password ถูกส่งกลับไปใน JSON response

            // เก็บ user_name ลงใน session
            session.setAttribute("user_name", loginUser.getUserName());

            // ส่งข้อมูลผู้ใช้กลับไปในรูปแบบ JSON
            return new ResponseEntity<>(existingUser, HttpStatus.OK);
        } else {
            // หากข้อมูลไม่ถูกต้อง ส่งข้อความผิดพลาด
            return new ResponseEntity<>("Login failed. Invalid credentials.", HttpStatus.UNAUTHORIZED);
        }
    }

}

