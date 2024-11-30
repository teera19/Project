package com.example.server_management.rest_controllers;

import com.example.server_management.models.Product;
import com.example.server_management.service.UserService;
import jakarta.servlet.http.HttpSession;
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
public class Myproduct {
    @Autowired
    private UserService userService;
    @GetMapping("/my-product")
    public ResponseEntity<List<Product>> getMyProducts(HttpSession session) {
        // ดึง user_name จาก session
        String userName = (String) session.getAttribute("user_name");

        if (userName == null) {
            // ถ้าไม่มี user_name ใน session (หมายความว่าผู้ใช้ยังไม่ได้ล็อกอิน)
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        try {
            // เรียกใช้ service เพื่อดึงสินค้าของผู้ใช้ที่ล็อกอิน
            List<Product> products = userService.getMyProducts(userName);

            // ตรวจสอบว่า products ว่างหรือไม่
            if (products.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            // ตัด Base64 ของแต่ละสินค้าที่มีภาพใน Base64 ให้เหลือแค่ 100 ตัวแรก
            for (Product product : products) {
                String base64Encoded = product.getImageBase64(); // ดึง Base64 จาก product

                if (base64Encoded != null && !base64Encoded.isEmpty()) {
                    // ตัด Base64 ให้เหลือแค่ 100 ตัวแรก
                    String shortBase64 = base64Encoded.substring(0, Math.min(base64Encoded.length(), 100));

                    // เปลี่ยน Base64 ที่ถูกตัดมาถูกต้องเป็น byte[]
                    byte[] decodedImage = Base64.getDecoder().decode(shortBase64);
                    product.setImage(decodedImage); // ตั้งค่า image เป็น byte[] ที่ decode แล้ว
                }
            }

            return new ResponseEntity<>(products, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
