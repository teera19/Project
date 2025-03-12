package com.example.server_management.rest_controllers;

import com.example.server_management.models.User;
import com.example.server_management.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
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
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=UTF-8");
        try {
            User existingUser = userRepository.findByUserName(loginUser.getUserName());

            if (existingUser == null) {
                return new ResponseEntity<>(Map.of(
                        "message", "User not found."
                ), HttpStatus.NOT_FOUND);
            }

            if (!existingUser.getPassword().equals(loginUser.getPassword())) {
                return new ResponseEntity<>(Map.of(
                        "message", "Invalid password."
                ), HttpStatus.UNAUTHORIZED);
            }

            session.setAttribute("user_id", existingUser.getUserId()); 
            session.setAttribute("user_name", existingUser.getUserName());

            existingUser.setPassword(null);

            return new ResponseEntity<>(existingUser, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(Map.of(
                    "message", "An error occurred while processing the request."
            ), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}



