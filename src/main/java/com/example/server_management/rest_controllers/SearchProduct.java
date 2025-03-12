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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
public class SearchProduct {
    @Autowired
    private UserService userService;

    @GetMapping("/search-products")
    public ResponseEntity<List<ProductResponse>> searchProducts(@RequestParam("query") String query) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=UTF-8");
        try {
            List<Product> products = userService.searchProductsByName(query);
            if (products.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            // ✅ แปลงจาก `Product` เป็น `ProductResponse`
            List<ProductResponse> productResponses = products.stream()
                    .map(ProductResponse::new)
                    .collect(Collectors.toList());

            return new ResponseEntity<>(productResponses, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/search-bycategory")
    public ResponseEntity<List<ProductResponse>> searchProductsByCategory(@RequestParam("category") String categoryName) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=UTF-8");
        try {
            List<Product> products = userService.getProductsByCategory(categoryName);
            if (products.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            // ✅ แปลงจาก `Product` เป็น `ProductResponse`
            List<ProductResponse> productResponses = products.stream()
                    .map(ProductResponse::new)
                    .collect(Collectors.toList());

            return new ResponseEntity<>(productResponses, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

