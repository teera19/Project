package com.example.server_management.rest_controllers;
import com.example.server_management.models.Category;
import com.example.server_management.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/categorie")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();

        // แปลงเป็น DTO เพื่อลดข้อมูลที่ไม่จำเป็น
        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(cat -> new CategoryDTO(cat.getCategoryId(), cat.getName()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(categoryDTOs);
    }

    // ✅ API สำหรับเพิ่มหมวดหมู่ใหม่
    @PostMapping("/add")
    public ResponseEntity<?> addCategory(@RequestBody CategoryDTO categoryDTO) {
        // ตรวจสอบว่าหมวดหมู่มีอยู่แล้วหรือไม่
        Optional<Category> existingCategory = Optional.ofNullable(categoryRepository.findByName(categoryDTO.getName()));

        if (existingCategory.isPresent()) {
            return ResponseEntity.badRequest().body("Category already exists: " + categoryDTO.getName());
        }

        // ถ้ายังไม่มี ให้สร้างใหม่
        Category newCategory = new Category();
        newCategory.setName(categoryDTO.getName());
        categoryRepository.save(newCategory);

        return ResponseEntity.ok("Category added successfully: " + categoryDTO.getName());
    }
}

// ✅ DTO สำหรับรับข้อมูลจากผู้ใช้
class CategoryDTO {
    private int category_id;
    private String name;

    public CategoryDTO() {} // Default Constructor

    public CategoryDTO(int category_id, String name) {
        this.category_id = category_id;
        this.name = name;
    }

    public int getCategory_id() { return category_id; }
    public String getName() { return name; }

    public void setCategory_id(int category_id) { this.category_id = category_id; }
    public void setName(String name) { this.name = name; }
}
