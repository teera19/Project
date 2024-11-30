package com.example.server_management.rest_controllers;

import com.example.server_management.models.Product;
import com.example.server_management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/")
public class Allproduct {

    @Autowired
    private UserService userService;

    @GetMapping("/all-product")
    public ResponseEntity<List<Product>> getAllProducts() {
        try {
            List<Product> products = userService.getAllProducts(); // เรียกใช้เมธอดจาก UserService

            // ตรวจสอบว่า products ว่างหรือไม่
            if (products.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            // ตัด Base64 ของแต่ละสินค้าที่มีภาพใน Base64 ให้เหลือแค่ 100 ตัวแรก
            for (Product product : products) {
                String base64Encoded = product.getImageBase64();
                if (base64Encoded != null && !base64Encoded.isEmpty()) {
                    // ตัด Base64 ให้เหลือแค่ 100 ตัวแรก
                    String shortBase64 = base64Encoded.substring(0, Math.min(base64Encoded.length(), 100));
                    product.setImage(Base64.getDecoder().decode(shortBase64)); // ใช้ข้อมูลที่ถูกตัดในส่วนนี้
                }
            }

            return new ResponseEntity<>(products, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

