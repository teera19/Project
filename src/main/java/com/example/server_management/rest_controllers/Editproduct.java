package com.example.server_management.rest_controllers;

import com.example.server_management.models.*;
import com.example.server_management.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/")
public class Editproduct {

    @Autowired
    private UserService userService;

    @PostMapping("/edit-product/{product_id}")
    public ResponseEntity<?> editProduct(
            @PathVariable("product_id") int productId,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") double price,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam("category_name") String categoryName,
            @RequestParam("defectDetails") String defectDetails,  // ✅ รองรับ defectDetails
            @RequestParam Map<String, String> details,
            HttpSession session) throws IOException {

        // 🛑 ตรวจสอบการล็อกอิน
        String userName = (String) session.getAttribute("user_name");
        if (userName == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "User not logged in"));
        }

        try {
            // ✅ ค้นหาสินค้า
            Product product = userService.findProductById(productId);
            if (product == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Product not found"));
            }

            // ✅ ค้นหา Category จากชื่อ
            Category category = userService.findCategoryByName(categoryName);
            if (category == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Invalid category: " + categoryName));
            }

            // ✅ อัปเดตข้อมูลสินค้า
            product.setCategory(category);
            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);
            product.setDefectDetails(defectDetails);  // ✅ อัปเดต defectDetails

            // ✅ อัปเดตข้อมูลหมวดหมู่ใน `details` โดยตรง
            details.put("category", category.getName());
            details.put("shopTitle", product.getShop().getTitle());

            // ✅ อัปเดตรูปภาพสินค้า
            if (image != null && !image.isEmpty()) {
                userService.updateProductImage(product, image.getBytes());
            }

            // ✅ บันทึกสินค้า
            userService.saveProduct(product);

            // ✅ คืนค่า `ProductResponse` ที่มี `defectDetails`
            return ResponseEntity.ok(new ProductResponse(product));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error updating product", "error", e.getMessage()));
        }
    }
}
