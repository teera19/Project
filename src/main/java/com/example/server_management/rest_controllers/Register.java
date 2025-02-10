package com.example.server_management.rest_controllers;

import com.example.server_management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class Register {

    @Autowired
    UserService userService;

    @PostMapping("/register")
    public ResponseEntity register(@RequestParam("user_name") String user_name,
                                   @RequestParam("name") String name,
                                   @RequestParam("last_name") String last_name,
                                   @RequestParam("email") String email,
                                   @RequestParam("password") String password,
                                   @RequestParam("address") String address,
                                   @RequestParam("tel") String tel) {

        System.out.println("Received Register Request: " + user_name + ", " + name + ", " + last_name + ", " + email);

        if (user_name.isEmpty() || name.isEmpty() || last_name.isEmpty() || email.isEmpty() || password.isEmpty() || address.isEmpty() || tel.isEmpty()) {
            System.out.println("Validation Failed: Some fields are empty.");
            return new ResponseEntity<>("Please Complete all Fields", HttpStatus.BAD_REQUEST);
        }
        if (userService.countByUserName(user_name) > 0) {
            System.out.println("Validation Failed: Username already exists.");
            return new ResponseEntity<>("Username already exists", HttpStatus.BAD_REQUEST);
        }

        int result = userService.registerServiceMethod(user_name, name, last_name, email, password, address, tel);

        if (result != 1) {
            System.out.println("Error: Failed to Register");
            return new ResponseEntity<>("Failed to Register", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        System.out.println("Register Successfully!");
        return new ResponseEntity<>("Register Successfully", HttpStatus.OK);
    }
}
