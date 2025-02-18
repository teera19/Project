package com.example.server_management.rest_controllers;

import com.example.server_management.dto.ResponseProduct;
import com.example.server_management.models.*;
import com.example.server_management.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;



    @RestController
    @RequestMapping("/")
    public class Editproduct {

        @Autowired
        private UserService userService;

        @PostMapping("/edit-product/{product_id}")
        public ResponseEntity<Object> editProduct(
                @PathVariable("product_id") int productId,
                @RequestParam("name") String name,
                @RequestParam("description") String description,
                @RequestParam("price") double price,
                @RequestParam(value = "image", required = false) MultipartFile image,
                @RequestParam("category_id") int categoryId,
                @RequestParam Map<String, String> details, // รับข้อมูล details ของหมวดหมู่
                HttpSession session) throws IOException {

            // ตรวจสอบการล็อกอิน
            String userName = (String) session.getAttribute("user_name");
            if (userName == null) {
                return new ResponseEntity<>("User not logged in", HttpStatus.FORBIDDEN);
            }

            byte[] imageBytes = null;
            if (image != null && !image.isEmpty()) {
                imageBytes = image.getBytes();
            }

            try {
                // ค้นหาสินค้าจาก productId
                Product product = userService.findProductById(productId);
                if (product == null) {
                    return new ResponseEntity<>("Product not found", HttpStatus.NOT_FOUND);
                }

                // ตรวจสอบและอัปเดต Category
                Category category = userService.findCategoryById(categoryId);
                if (category == null) {
                    return new ResponseEntity<>("Invalid category_id: " + categoryId, HttpStatus.BAD_REQUEST);
                }
                product.setCategory(category); // อัปเดตหมวดหมู่ใหม่ใน Product

                // อัปเดตข้อมูลทั่วไปของสินค้า
                product.setName(name);
                product.setDescription(description);
                product.setPrice(price);

                Object detailObject = null;

                // จัดการรายละเอียดตามหมวดหมู่
                if (categoryId == 1) { // สำหรับเสื้อผ้า
                    detailObject = updateClothingDetails(product, details);
                } else if (categoryId == 2) { // สำหรับโทรศัพท์
                    detailObject = updatePhoneDetails(product, details);
                } else if (categoryId == 3) { // สำหรับรองเท้า
                    detailObject = updateShoesDetails(product, details);
                }

                // หากมีการอัปโหลดภาพใหม่
                if (imageBytes != null) {
                    userService.saveCompressedImage(imageBytes, product.getProductId());
                    String imageUrl = "https://project-production-f4db.up.railway.app/images/" + product.getProductId() + ".jpg";
                    product.setImageUrl(imageUrl);

                }

                // บันทึกสินค้า
                userService.saveProduct(product);

                String imageUrl = "https://project-production-f4db.up.railway.app/images/" + product.getProductId() + ".jpg";


                // ส่ง ResponseProduct กลับ
                return new ResponseEntity<>(new ResponseProduct(
                        product.getProductId(),
                        product.getName(),
                        product.getDescription(),
                        product.getPrice(),
                        imageUrl,
                        detailObject // รายละเอียดเฉพาะหมวดหมู่
                ), HttpStatus.OK);

            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseEntity<>("Error updating product: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        private ClothingDetails updateClothingDetails(Product product, Map<String, String> details) {
            ClothingDetails clothingDetails = userService.findClothingDetailsByProductId(product.getProductId());
            if (clothingDetails == null) {
                clothingDetails = new ClothingDetails();
                clothingDetails.setProduct(product);
            }

            String tearLocation = details.getOrDefault("details[tear_location]", "Unknown");
            boolean hasStain = parseBooleanOrDefault(details.get("details[has_stain]"), false);
            int repairCount = parseIntOrDefault(details.get("details[repair_count]"), 0);

            clothingDetails.setTearLocation(tearLocation);
            clothingDetails.setHasStain(hasStain);
            clothingDetails.setRepairCount(repairCount);

            userService.saveClothingDetails(clothingDetails);
            return clothingDetails;
        }

        private PhoneDetails updatePhoneDetails(Product product, Map<String, String> details) {
            PhoneDetails phoneDetails = userService.findPhoneDetailsByProductId(product.getProductId());
            if (phoneDetails == null) {
                phoneDetails = new PhoneDetails();
                phoneDetails.setProduct(product);
            }

            boolean basicFunctionalityStatus = parseBooleanOrDefault(details.get("details[basic_functionality_status]"), false);
            String nonFunctionalParts = details.getOrDefault("details[nonfunctional_parts]", "Unknown");
            String batteryStatus = details.getOrDefault("details[battery_status]", "Unknown");
            int scratchCount = parseIntOrDefault(details.get("details[scratch_count]"), 0);

            phoneDetails.setBasicFunctionalityStatus(basicFunctionalityStatus);
            phoneDetails.setNonFunctionalParts(nonFunctionalParts);
            phoneDetails.setBatteryStatus(batteryStatus);
            phoneDetails.setScratchCount(scratchCount);

            userService.savePhoneDetails(phoneDetails);
            return phoneDetails;
        }

        private ShoesDetails updateShoesDetails(Product product, Map<String, String> details) {
            ShoesDetails shoesDetails = userService.findShoesDetailsByProductId(product.getProductId());
            if (shoesDetails == null) {
                shoesDetails = new ShoesDetails();
                shoesDetails.setProduct(product);
            }

            boolean hasBrandLogo = parseBooleanOrDefault(details.get("details[hasbrand_logo]"), false);
            int repairCount = parseIntOrDefault(details.get("details[repair_count]"), 0);
            String tearLocation = details.getOrDefault("details[tear_location]", "Unknown");

            shoesDetails.setHasBrandLogo(hasBrandLogo);
            shoesDetails.setRepairCount(repairCount);
            shoesDetails.setTearLocation(tearLocation);

            userService.saveShoesDetails(shoesDetails);
            return shoesDetails;
        }

        private boolean parseBooleanOrDefault(String value, boolean defaultValue) {
            if (value == null || value.trim().isEmpty()) {
                return defaultValue;
            }
            return Boolean.parseBoolean(value);
        }

        private int parseIntOrDefault(String value, int defaultValue) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
    }

