package com.example.server_management.rest_controllers;

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

import java.io.IOException;
import java.util.Base64;

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
                                             @RequestParam("category_id") int categoryId) throws IOException {

            // ตรวจสอบว่าไฟล์มีขนาดไม่เกินที่กำหนด
            if (image.isEmpty()) {
                return new ResponseEntity<>("No image uploaded", HttpStatus.BAD_REQUEST);
            }

            // แปลงไฟล์ image เป็น byte[]
            byte[] imageBytes = image.getBytes();

            // เรียกใช้ userService เพื่อเพิ่มข้อมูลสินค้า
            Product addedProduct = userService.addProductToShop(shopTitle, name, description, price, imageBytes, categoryId);

            String categoryName = addedProduct.getCategory().getName();
            addedProduct.setCategoryName(categoryName);
            // แปลง byte array ของ image เป็น Base64
            String base64Encoded = addedProduct.getImageBase64();

            if (base64Encoded != null) {
                // ตัด Base64 ให้แสดงแค่ 100 ตัวแรก
                String shortBase64 = base64Encoded.substring(0, Math.min(base64Encoded.length(), 100));
                addedProduct.setImage(Base64.getDecoder().decode(shortBase64));
            }

            // ส่งข้อมูลสินค้าเป็น JSON
            return new ResponseEntity<>(addedProduct, HttpStatus.CREATED);

    }

}
