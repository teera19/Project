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


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/")
public class Myproduct {

    @Autowired
    private UserService userService;

    @GetMapping("/my-product")
    public ResponseEntity<?> getMyProducts(HttpSession session) {
        // ดึง user_name จาก session
        String userName = (String) session.getAttribute("user_name");

        // ตรวจสอบว่า session มี user_name หรือไม่
        if (userName == null) {
            return new ResponseEntity<>(Map.of(
                    "message", "User not logged in. Please log in first."
            ), HttpStatus.FORBIDDEN);
        }

        try {
            // ดึงรายการสินค้า
            List<Product> products = userService.getMyProducts(userName);

            if (products.isEmpty()) {
                // กรณีไม่มีสินค้า
                return new ResponseEntity<>(Map.of(
                        "message", "Don't have product"
                ), HttpStatus.OK); // สามารถเปลี่ยนเป็น HttpStatus.NO_CONTENT ได้หากเหมาะสม
            }

            // แปลง URL ภาพสินค้า และลบข้อมูล byte[] ออกจาก Response
            for (Product product : products) {
                String imageUrl = "/images/" + product.getProductId() + ".jpg";
                product.setImageUrl(imageUrl);
                product.setImage(null); // ลบข้อมูล byte[] ออกจาก JSON Response
            }

            // ส่งรายการสินค้า
            return new ResponseEntity<>(products, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(Map.of(
                    "message", "Internal server error occurred."
            ), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
