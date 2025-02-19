package com.example.server_management.rest_controllers;

import com.example.server_management.dto.ResponseProduct;
import com.example.server_management.models.Product;
import com.example.server_management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/")
public class Addproduct {

    @Autowired
    private UserService userService;

    @PostMapping("/add-product")
    public ResponseEntity<Object> addProduct(@RequestParam("shop_title") String shopTitle,
                                             @RequestParam("name") String name,
                                             @RequestParam("description") String description,
                                             @RequestParam("price") double price,
                                             @RequestParam("image") MultipartFile image,
                                             @RequestParam("category_id") String categoryIdStr, // 🛠 รับค่าเป็น String
                                             @RequestParam Map<String, String> details) throws IOException {
        try {
            if (image.isEmpty()) {
                return new ResponseEntity<>("No image uploaded", HttpStatus.BAD_REQUEST);
            }

            // ✅ แปลง category_id เป็น int
            int categoryId = parseIntOrDefault(categoryIdStr, 0);
            System.out.println("📌 Received category_id: " + categoryId);

            if (categoryId <= 0) {
                return new ResponseEntity<>("Invalid category_id: " + categoryId, HttpStatus.BAD_REQUEST);
            }

            ResponseProduct responseProduct = userService.addProductToShop(
                    shopTitle, name, description, price, image, categoryId, details);

            return new ResponseEntity<>(responseProduct, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Internal Server Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    private int parseIntOrDefault(String value, int defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }


}