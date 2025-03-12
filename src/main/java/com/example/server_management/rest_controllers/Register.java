package com.example.server_management.rest_controllers;

import com.example.server_management.models.User;
import com.example.server_management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class Register {

    @Autowired
    UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestParam String userName,
            @RequestParam String name,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String address,
            @RequestParam String tel) {


        System.out.println("Received Register Request: " + userName);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=UTF-8");

        if (userName.isEmpty() || name.isEmpty() || lastName.isEmpty() || email.isEmpty() ||
                password.isEmpty() || address.isEmpty() || tel.isEmpty()) {
            return ResponseEntity.badRequest().body("Please Complete all Fields");
        }

        if (userService.countByUserName(userName) > 0) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        //  ต้องสร้าง User object ก่อนบันทึก
        User newUser = new User();
        newUser.setUserName(userName);
        newUser.setName(name);
        newUser.setLastName(lastName);
        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setAddress(address);
        newUser.setTel(tel);

        //  ใช้ `registerServiceMethod` ในการบันทึก
        User savedUser = userService.registerServiceMethod(newUser);

        if (savedUser == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to Register");
        }

        return ResponseEntity.ok("Register Successfully");
    }
}