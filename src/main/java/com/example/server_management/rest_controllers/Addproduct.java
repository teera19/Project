package com.example.server_management.rest_controllers;

import com.example.server_management.dto.ResponseProduct;
import com.example.server_management.models.Category;
import com.example.server_management.models.ProductResponse;
import com.example.server_management.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
public class Addproduct {

    @Autowired
    private UserService userService;

    @PostMapping(value = "/add-product", consumes = {"multipart/form-data"})
    public ResponseEntity<Object> addProduct(@RequestParam("shop_title") String shopTitle,
                                             @RequestParam("name") String name,
                                             @RequestParam("description") String description,
                                             @RequestParam("price") double price,
                                             @RequestParam(value = "image", required = false) MultipartFile image,
                                             @RequestParam("category_name") String categoryName,
                                             @RequestParam("defectDetails") String defectDetails,
                                             @RequestParam Map<String, String> details,
                                             HttpSession session) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=UTF-8");
        try {
            // ✅ ตรวจสอบว่าผู้ใช้ล็อกอินหรือไม่
            String userName = (String) session.getAttribute("user_name");
            if (userName == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Please log in to add a product."));
            }

            // ✅ ตรวจสอบค่า `defectDetails`
            if (defectDetails == null || defectDetails.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "defectDetails must not be empty."));
            }

            // ✅ หา categoryId จาก categoryName
            Category category = userService.findCategoryByName(categoryName);
            if (category == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Invalid category name: " + categoryName));
            }

            // ✅ เพิ่ม defectDetails ลงใน `details`
            Map<String, String> updatedDetails = new HashMap<>(details);
            updatedDetails.put("defectDetails", defectDetails);

            // ✅ เพิ่มสินค้าเข้าไปยังฐานข้อมูล
            ResponseProduct responseProduct = userService.addProductToShop(
                    shopTitle, name, description, price, image, category.getCategoryId(), updatedDetails);

            // ✅ สร้าง JSON Response
            ProductResponse productResponse = new ProductResponse(responseProduct);
            return ResponseEntity.status(HttpStatus.CREATED).body(productResponse);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "An internal server error occurred.", "error", e.getMessage()));
        }
    }
}
