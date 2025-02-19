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
            @RequestParam("category_name") String categoryName,  // ✅ เปลี่ยนเป็น category_name
            @RequestParam Map<String, String> details,
            HttpSession session) throws IOException {

        // ✅ Log เพื่อตรวจสอบค่าที่รับเข้ามา
        System.out.println("📌 productId: " + productId);
        System.out.println("📌 category_name: " + categoryName);

        // ✅ ตรวจสอบการล็อกอิน
        String userName = (String) session.getAttribute("user_name");
        if (userName == null) {
            return new ResponseEntity<>("User not logged in", HttpStatus.FORBIDDEN);
        }

        try {
            // ✅ ค้นหาสินค้า
            Product product = userService.findProductById(productId);
            if (product == null) {
                System.out.println("❌ Product not found: " + productId);
                return new ResponseEntity<>("Product not found", HttpStatus.NOT_FOUND);
            }

            // ✅ หา Category จากชื่อ
            Category category = userService.findCategoryByName(categoryName);
            if (category == null) {
                System.out.println("❌ Category not found: " + categoryName);
                return new ResponseEntity<>("Invalid category: " + categoryName, HttpStatus.BAD_REQUEST);
            }

            // ✅ อัปเดตข้อมูลสินค้า
            product.setCategory(category);
            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);

            Object detailObject = null;

            // ✅ อัปเดตรายละเอียดสินค้าแยกตามหมวดหมู่
            switch (category.getCategoryId()) {
                case 1:
                    detailObject = updateClothingDetails(product, details);
                    break;
                case 2:
                    detailObject = updatePhoneDetails(product, details);
                    break;
                case 3:
                    detailObject = updateShoesDetails(product, details);
                    break;
                default:
                    System.out.println("⚠️ No additional details required for category: " + categoryName);
            }

            // ✅ อัปเดตรูปภาพสินค้า
            if (image != null && !image.isEmpty()) {
                byte[] imageBytes = image.getBytes();
                userService.saveCompressedImage(imageBytes, product.getProductId());
                String imageUrl = "https://project-production-f4db.up.railway.app/images/" + product.getProductId() + ".jpg";
                product.setImageUrl(imageUrl);
            }

            // ✅ บันทึกสินค้า
            userService.saveProduct(product);

            return new ResponseEntity<>(new ResponseProduct(
                    product.getProductId(),
                    product.getName(),
                    product.getDescription(),
                    product.getPrice(),
                    product.getImageUrl(),
                    detailObject
            ), HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error updating product: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ✅ ฟังก์ชันอัปเดตรายละเอียดของเสื้อผ้า
    private ClothingDetails updateClothingDetails(Product product, Map<String, String> details) {
        ClothingDetails clothingDetails = userService.findClothingDetailsByProductId(product.getProductId());
        if (clothingDetails == null) {
            clothingDetails = new ClothingDetails();
            clothingDetails.setProduct(product);
        }

        clothingDetails.setTearLocation(details.getOrDefault("details[tear_location]", "Unknown"));
        clothingDetails.setHasStain(details.getOrDefault("details[has_stain]", "ไม่มี"));
        clothingDetails.setRepairCount(parseIntOrDefault(details.get("details[repair_count]"), 0));

        userService.saveClothingDetails(clothingDetails);
        return clothingDetails;
    }

    // ✅ ฟังก์ชันอัปเดตรายละเอียดของโทรศัพท์
    private PhoneDetails updatePhoneDetails(Product product, Map<String, String> details) {
        PhoneDetails phoneDetails = userService.findPhoneDetailsByProductId(product.getProductId());
        if (phoneDetails == null) {
            phoneDetails = new PhoneDetails();
            phoneDetails.setProduct(product);
        }

        phoneDetails.setBasicFunctionalityStatus(details.getOrDefault("details[basic_functionality_status]", "ไม่ระบุ"));
        phoneDetails.setNonFunctionalParts(details.getOrDefault("details[nonfunctional_parts]", "Unknown"));
        phoneDetails.setBatteryStatus(details.getOrDefault("details[battery_status]", "Unknown"));
        phoneDetails.setScratchCount(parseIntOrDefault(details.get("details[scratch_count]"), 0));

        userService.savePhoneDetails(phoneDetails);
        return phoneDetails;
    }

    // ✅ ฟังก์ชันอัปเดตรายละเอียดของรองเท้า
    private ShoesDetails updateShoesDetails(Product product, Map<String, String> details) {
        ShoesDetails shoesDetails = userService.findShoesDetailsByProductId(product.getProductId());
        if (shoesDetails == null) {
            shoesDetails = new ShoesDetails();
            shoesDetails.setProduct(product);
        }

        shoesDetails.setHasBrandLogo(details.getOrDefault("details[hasbrand_logo]", "ไม่มี"));
        shoesDetails.setRepairCount(parseIntOrDefault(details.get("details[repair_count]"), 0));
        shoesDetails.setTearLocation(details.getOrDefault("details[tear_location]", "Unknown"));

        userService.saveShoesDetails(shoesDetails);
        return shoesDetails;
    }

    // ✅ ฟังก์ชันช่วยแปลง String เป็น int
    private int parseIntOrDefault(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}


