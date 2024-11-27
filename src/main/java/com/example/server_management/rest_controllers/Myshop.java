package com.example.server_management.rest_controllers;

import com.example.server_management.models.ProductResponse;
import com.example.server_management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/")
public class Myshop {

    @Autowired
    private UserService userService;

    @PostMapping("/create-shop")
    public ResponseEntity<String> createShop(@RequestParam("user_name") String user_name,
                                             @RequestParam("title") String title,
                                             @RequestParam("detail") String detail) {

        if (user_name.isEmpty() || title.isEmpty() || detail.isEmpty()) {
            return new ResponseEntity<>("Please Complete all Fields", HttpStatus.BAD_REQUEST);
        }

        userService.createShopForUser(user_name, title, detail);
        return new ResponseEntity<>("Shop Created Successfully", HttpStatus.OK);
    }
}
