package com.example.server_management.rest_controllers;

import com.example.server_management.models.Product;
import com.example.server_management.models.ProductResponse;
import com.example.server_management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
public class Allproduct {

    @Autowired
    private UserService userService;

    @GetMapping(value = "/all-product", produces = "application/json;charset=UTF-8")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=UTF-8");
        try {
            List<Product> products = userService.getAllProducts();
            if (products.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            List<ProductResponse> productResponses = products.stream()
                    .map(ProductResponse::new)  // ✅ ใช้ `ProductResponse`
                    .collect(Collectors.toList());

            return new ResponseEntity<>(productResponses, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}