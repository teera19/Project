package com.example.server_management.rest_controllers;

import com.example.server_management.models.Category;
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

    // Endpoint สำหรับแก้ไขสินค้าโดยใช้ product_id จาก Path Variable
    @PostMapping("/edit-product/{product_id}")
    public ResponseEntity<Object> editProduct(@PathVariable("product_id") int productId,
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
            // ค้นหาสินค้าจาก product_id
            Product product = userService.findProductById(productId);
            if (product == null) {
                return new ResponseEntity<>("Product not found", HttpStatus.NOT_FOUND);
            }

            // อัปเดตข้อมูลสินค้า
            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);

            // ค้นหาและอัปเดตหมวดหมู่
            Category category = userService.findCategoryById(categoryId);
            if (category == null) {
                return new ResponseEntity<>("Category not found", HttpStatus.NOT_FOUND);
            }
            product.setCategory(category);

            // หากมีการอัปโหลดภาพใหม่
            if (imageBytes != null) {
                String imageUrl = "/images/" + product.getProductId() + ".jpg";
                userService.saveCompressedImage(imageBytes, product.getProductId());
                product.setImageUrl(imageUrl);
            }

            // บันทึกสินค้า
            userService.saveProduct(product);

            return new ResponseEntity<>(product, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error updating product: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

