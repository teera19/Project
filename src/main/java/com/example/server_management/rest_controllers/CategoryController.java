package com.example.server_management.rest_controllers;

import com.example.server_management.models.Category;
import com.example.server_management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/categorie")
public class CategoryController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<Category> categories = userService.getAllCategories();

        // แปลงเป็น DTO เพื่อลดข้อมูลที่ไม่จำเป็น
        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(cat -> new CategoryDTO(cat.getCategoryId(), cat.getName()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(categoryDTOs);
    }
}

//  สร้าง DTO สำหรับส่งข้อมูล
class CategoryDTO {
    private int category_id;
    private String name;

    public CategoryDTO(int category_id, String name) {
        this.category_id = category_id;
        this.name = name;
    }

    public int getCategory_id() { return category_id; }
    public String getName() { return name; }
}