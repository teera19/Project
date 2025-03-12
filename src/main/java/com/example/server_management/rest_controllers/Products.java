package com.example.server_management.rest_controllers;

import com.example.server_management.models.Category;
import com.example.server_management.models.Product;
import com.example.server_management.models.ProductResponse;
import com.example.server_management.repository.CategoryRepository;
import com.example.server_management.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;

@RestController
@RequestMapping("/products")
public class Products {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // ✅ ดึงสินค้าตามหมวดหมู่
    @GetMapping(value = "/by-category/{categoryName}",produces = "application/json;charset=UTF-8")
    public ResponseEntity<?> getProductsByCategory(@PathVariable String categoryName) {
        Category category = categoryRepository.findByName(categoryName);
        if (category == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Category not found: " + categoryName));
        }

        List<Product> products = productRepository.findByCategory(category);
        List<ProductResponse> productResponses = products.stream()
                .map(product -> new ProductResponse(product))  // ✅ ใช้ Lambda สร้าง ProductResponse
                .collect(Collectors.toList());

        return ResponseEntity.ok(productResponses);
    }

    // ✅ ดึงสินค้าตาม ID
    @GetMapping(value = "/{productId}",produces = "application/json;charset=UTF-8")
    public ResponseEntity<?> getProductById(@PathVariable int productId) {
        Optional<Product> productOpt = productRepository.findById(productId);

        if (productOpt.isPresent()) {
            ProductResponse productResponse = new ProductResponse(productOpt.get());
            return ResponseEntity.ok(productResponse); // ✅ คืนค่า ProductResponse
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "ไม่มีสินค้า", "productId", productId));
        }
    }
}
