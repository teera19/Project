package com.example.server_management.rest_controllers;

import com.example.server_management.models.Product;
import com.example.server_management.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

@RestController
@RequestMapping("/")
public class Editproduct {

    @Autowired
    private UserService userService;

    @PostMapping("/edit-product")
    public ResponseEntity<Object> editProduct(@RequestParam("product_id") int productId,
                                              @RequestParam("shop_title") String shopTitle,
                                              @RequestParam("name") String name,
                                              @RequestParam("description") String description,
                                              @RequestParam("price") double price,
                                              @RequestParam("image") MultipartFile image,
                                              @RequestParam("category_id") int categoryId,
                                              HttpSession session) throws IOException {

        // ดึง user_name จาก session
        String userName = (String) session.getAttribute("user_name");

        if (userName == null) {
            return new ResponseEntity<>("User not logged in", HttpStatus.FORBIDDEN);
        }

        // ตรวจสอบว่าไฟล์มีขนาดไม่เกินที่กำหนด
        if (image.isEmpty()) {
            return new ResponseEntity<>("No image uploaded", HttpStatus.BAD_REQUEST);
        }

        // แปลงไฟล์ image เป็น byte[]
        byte[] imageBytes = image.getBytes();

        try {
            // เรียกใช้ userService เพื่ออัพเดตข้อมูลสินค้า
            Product updatedProduct = userService.editProduct(productId, shopTitle, name, description, price, imageBytes, categoryId);

            // แปลง byte array ของ image เป็น Base64
            String base64Encoded = updatedProduct.getImageBase64();

            if (base64Encoded != null) {
                // ตัด Base64 ให้แสดงแค่ 100 ตัวแรก
                String shortBase64 = base64Encoded.substring(0, Math.min(base64Encoded.length(), 100));
                updatedProduct.setImage(Base64.getDecoder().decode(shortBase64));
            }

            return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error updating product", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

