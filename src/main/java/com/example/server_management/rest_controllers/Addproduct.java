package com.example.server_management.rest_controllers;

import com.example.server_management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/")
public class Addproduct {
    @Autowired
    private UserService userService;
    @PostMapping("/add-product")
    public ResponseEntity<String> addProduct(@RequestParam("shop_title") String shopTitle,
                                             @RequestParam("name") String name,
                                             @RequestParam("description") String description,
                                             @RequestParam("price") double price,
                                             @RequestParam("image") MultipartFile image) {
        try {
            // ตรวจสอบว่าไฟล์มีขนาดไม่เกินที่กำหนด
            if (image.isEmpty()) {
                return new ResponseEntity<>("No image uploaded", HttpStatus.BAD_REQUEST);
            }

            // แปลงไฟล์ image เป็น byte[]
            byte[] imageBytes = image.getBytes();

            // เรียกใช้ userService เพื่อเพิ่มข้อมูลสินค้า พร้อมส่ง imageBytes
            userService.addProductToShop(shopTitle, name, description, price, imageBytes);

            return new ResponseEntity<>("Product added successfully", HttpStatus.OK);
        } catch (IOException e) {
            // การจัดการข้อผิดพลาดในกรณีที่เกิด IOException
            return new ResponseEntity<>("Failed to process the image", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
