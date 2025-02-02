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

        // ตรวจสอบว่าผู้ใช้ล็อกอินหรือไม่
        if (userName == null) {
            return new ResponseEntity<>(Map.of(
                    "message", "User not logged in. Please log in first."
            ), HttpStatus.FORBIDDEN);
        }

        try {
            // ดึงรายการสินค้าของผู้ใช้
            List<Product> products = userService.getMyProducts(userName);

            // ตรวจสอบว่ามีสินค้าในรายการหรือไม่
            if (products.isEmpty()) {
                return new ResponseEntity<>(Map.of(
                        "message", "Don't have any products"
                ), HttpStatus.OK);
            }

            //ไม่ส่งข้อมูลร้านค้า
            List<ProductResponse> productResponses = products.stream()
                    .map(product -> new ProductResponse(product, false)) // ไม่แสดง shopTitle, shopDetail
                    .collect(Collectors.toList());

            return new ResponseEntity<>(productResponses, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(Map.of(
                    "message", "Internal server error occurred."
            ), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

