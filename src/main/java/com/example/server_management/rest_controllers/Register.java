package com.example.server_management.rest_controllers;

import com.example.server_management.models.User;
import com.example.server_management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class Register {

    @Autowired
    UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        System.out.println("Received Register Request: " + user.getUserName() + ", " + user.getName() + ", " + user.getLastName() + ", " + user.getEmail());

        if (user.getUserName().isEmpty() || user.getName().isEmpty() || user.getLastName().isEmpty() ||
                user.getEmail().isEmpty() || user.getPassword().isEmpty() ||
                user.getAddress().isEmpty() || user.getTel().isEmpty()) {
            System.out.println("Validation Failed: Some fields are empty.");
            return ResponseEntity.badRequest().body("Please Complete all Fields");
        }

        if (userService.countByUserName(user.getUserName()) > 0) {
            System.out.println("Validation Failed: Username already exists.");
            return ResponseEntity.badRequest().body("Username already exists");
        }

        User savedUser = userService.registerServiceMethod(user);

        if (savedUser == null) {
            System.out.println("Error: Failed to Register");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to Register");
        }

        System.out.println("Register Successfully!");
        return ResponseEntity.ok("Register Successfully");
    }
}
