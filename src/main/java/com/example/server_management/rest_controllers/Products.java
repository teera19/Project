package com.example.server_management.rest_controllers;

import com.example.server_management.models.Category;
import com.example.server_management.models.Product;
import com.example.server_management.models.ProductResponse;
import com.example.server_management.repository.CategoryRepository;
import com.example.server_management.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
public class Products {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("/by-category/{categoryName}")
    public ResponseEntity<?> getProductsByCategory(@PathVariable String categoryName) {
        //  ค้นหา Category ตามชื่อ
        Category category = categoryRepository.findByName(categoryName);
        if (category == null) {
            return new ResponseEntity<>("Category not found: " + categoryName, HttpStatus.NOT_FOUND);
        }

        //  ค้นหาสินค้าทั้งหมดที่อยู่ใน Category นี้
        List<Product> products = productRepository.findByCategory(category);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable int productId) {
        return productRepository.findById(productId)
                .map(product -> ResponseEntity.ok(new ProductResponse(product))) // ✅ ถ้าพบสินค้า แสดง `ProductResponse`
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)); // ✅ ถ้าไม่พบสินค้า คืน `null`
    }

}

