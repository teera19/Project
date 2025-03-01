package com.example.server_management.rest_controllers;

import com.example.server_management.models.Product;
import com.example.server_management.models.ProductResponse;
import com.example.server_management.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
public class Myproduct {

    @Autowired
    private UserService userService;

    @GetMapping("/my-product")
    public ResponseEntity<?> getMyProducts(HttpSession session) {
        String userName = (String) session.getAttribute("user_name");

        if (userName == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Please log in to view your products."));
        }

        try {
            List<Product> products = userService.getMyProducts(userName);

            // ✅ If no products, return a friendly message with `200 OK`
            if (products.isEmpty()) {
                return ResponseEntity.ok(Map.of("message", "You have no products for sale."));
            }

            List<ProductResponse> productResponses = products.stream()
                    .map(ProductResponse::new)  // Convert Product to ProductResponse
                    .collect(Collectors.toList());

            return ResponseEntity.ok(productResponses);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "An internal server error occurred. Please try again later."));
        }
    }
}

