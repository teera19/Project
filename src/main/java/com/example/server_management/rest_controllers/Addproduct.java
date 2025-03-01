package com.example.server_management.rest_controllers;

import com.example.server_management.dto.ResponseProduct;
import com.example.server_management.models.Category;
import com.example.server_management.service.CloudinaryService;
import com.example.server_management.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;


@RestController
@RequestMapping("/")
public class Addproduct {

    @Autowired
    private UserService userService;

    @Autowired
    private CloudinaryService cloudinaryService; // ✅ ใช้ Cloudinary เหมือน addAuction

    @PostMapping("/add-product")
    public ResponseEntity<Object> addProduct(@RequestParam("shop_title") String shopTitle,
                                             @RequestParam("name") String name,
                                             @RequestParam("description") String description,
                                             @RequestParam("price") double price,
                                             @RequestParam(value = "image", required = false) MultipartFile image, // ✅ ปรับให้ไม่จำเป็น
                                             @RequestParam("category_name") String categoryName,
                                             @RequestParam Map<String, String> details,
                                             HttpSession session) {
        try {
            System.out.println("📌 Category: " + categoryName);

            String userName = (String) session.getAttribute("user_name");
            if (userName == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Please log in to add a product."));
            }

            // ✅ หา category_id ตามชื่อ
            Category category = userService.findCategoryByName(categoryName);
            if (category == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Invalid category name: " + categoryName));
            }

            System.out.println("✅ Found category ID: " + category.getCategoryId());

            // ✅ บันทึกสินค้าโดยอัปโหลดภาพขึ้น Cloudinary
            ResponseProduct responseProduct = userService.addProductToShop(
                    shopTitle, name, description, price, image, category.getCategoryId(), details);

            return ResponseEntity.status(HttpStatus.CREATED).body(responseProduct);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "An internal server error occurred.", "error", e.getMessage()));
        }
    }
}
