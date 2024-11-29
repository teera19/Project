package com.example.server_management.rest_controllers;

import com.example.server_management.models.User;
import com.example.server_management.repository.UserRepository;
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
    public ResponseEntity<?> loginUser(@RequestBody User loginUser) {

        User existingUser = userRepository.findByUserName(loginUser.getUserName());

        if (existingUser != null && loginUser.getPassword().equals(existingUser.getPassword())) {
            existingUser.setPassword(null);


            return new ResponseEntity<>(existingUser, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Login failed. Invalid credentials.", HttpStatus.UNAUTHORIZED);
        }
    }
}

