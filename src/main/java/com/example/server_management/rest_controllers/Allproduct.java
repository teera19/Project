package com.example.server_management.rest_controllers;

import com.example.server_management.models.ProductResponse;
import com.example.server_management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/")
public class Allproduct {
    @Autowired
    private UserService userService;
    @GetMapping("/all-product")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> response = userService.getAllProducts();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
