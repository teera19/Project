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
                                              @RequestParam(value = "image", required = false) MultipartFile image,
                                              @RequestParam("category_id") int categoryId,
                                              HttpSession session) throws IOException {

        // ดึง user_name จาก session
        String userName = (String) session.getAttribute("user_name");

        if (userName == null) {
            return new ResponseEntity<>("User not logged in", HttpStatus.FORBIDDEN);
        }

        byte[] imageBytes = null;
        if (image != null && !image.isEmpty()) {
            imageBytes = image.getBytes();
        }

        try {
            // เรียกใช้ userService เพื่ออัพเดตข้อมูลสินค้า
            Product updatedProduct = userService.editProduct(productId, shopTitle, name, description, price, imageBytes, categoryId);

            // ตั้งค่า URL ของภาพแทนการตัด Base64
            updatedProduct.setImageUrl("/images/" + updatedProduct.getProductId() + ".jpg");

            // ลบข้อมูล byte[] เพื่อไม่ส่งข้อมูลภาพใหญ่ไปใน JSON Response
            updatedProduct.setImage(null);

            return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error updating product: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

