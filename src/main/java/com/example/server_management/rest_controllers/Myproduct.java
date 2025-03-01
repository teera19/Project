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
                    .body(Map.of("message", "กรุณาเข้าสู่ระบบเพื่อดูสินค้าของคุณ"));
        }

        try {
            System.out.println("🔍 ตรวจสอบผู้ใช้: " + userName); // ✅ ตรวจสอบข้อมูล session

            List<Product> products = userService.getMyProducts(userName);

            // ✅ ถ้าไม่มีสินค้า ให้ส่งข้อความแจ้งเตือน
            if (products == null || products.isEmpty()) {
                System.out.println("⚠️ ไม่พบสินค้าสำหรับผู้ใช้: " + userName);
                return ResponseEntity.ok(Map.of("message", "คุณไม่มีสินค้าที่กำลังจำหน่าย"));
            }

            // ✅ แปลงข้อมูลเป็น DTO อย่างปลอดภัย
            List<ProductResponse> productResponses = products.stream()
                    .map(product -> {
                        try {
                            return new ProductResponse(product);
                        } catch (Exception ex) {
                            System.err.println("❌ เกิดข้อผิดพลาดในการแปลงสินค้า ID: " + product.getProductId());
                            ex.printStackTrace();
                            return null; // ป้องกันไม่ให้ API ล่ม
                        }
                    })
                    .filter(p -> p != null) // ลบค่าที่เป็น null ออกจากรายการ
                    .collect(Collectors.toList());

            return ResponseEntity.ok(productResponses);
        } catch (Exception e) {
            System.err.println("❌ เกิดข้อผิดพลาดขณะดึงข้อมูลสินค้าสำหรับผู้ใช้: " + userName);
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "เกิดข้อผิดพลาดภายในเซิร์ฟเวอร์ กรุณาลองใหม่อีกครั้ง"));
        }
    }
}
