package com.example.server_management.rest_controllers;

import com.example.server_management.dto.EditProfile;
import com.example.server_management.models.User;
import com.example.server_management.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/")
public class Editprofile {

    @Autowired
    private UserRepository userRepository;

    @PostMapping(value = "/edit-profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> editProfile(@ModelAttribute EditProfile editProfile, HttpSession session) {
        // ตรวจสอบว่า user ล็อกอินอยู่หรือไม่
        String userName = (String) session.getAttribute("user_name");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=UTF-8");
        if (userName == null) {
            return new ResponseEntity<>("User is not logged in.", HttpStatus.UNAUTHORIZED);
        }

        // ค้นหา user ใน database
        User existingUser = userRepository.findByUserName(userName);
        if (existingUser == null) {
            return new ResponseEntity<>("User not found.", HttpStatus.NOT_FOUND);
        }

        try {
            // ถ้ามีการอัปโหลดรูปภาพใหม่
            MultipartFile profileImage = editProfile.getProfileImage();
            if (profileImage != null && !profileImage.isEmpty()) {
                byte[] originalImageBytes = profileImage.getBytes();

                // หากต้องการบีบอัดรูปภาพ
                byte[] compressedImageBytes = compressImage(originalImageBytes);

                // สร้างชื่อไฟล์ใหม่และบันทึกไฟล์
                String fileName = UUID.randomUUID().toString() + "_" + profileImage.getOriginalFilename();
                Path filePath = Paths.get("uploads", fileName);
                Files.createDirectories(filePath.getParent());
                Files.write(filePath, compressedImageBytes);

                // อัปเดตเส้นทางรูปภาพใหม่
                existingUser.setProfileImage(fileName);
            }

            // ตรวจสอบแต่ละฟิลด์: หากส่งค่าใหม่ให้เปลี่ยน, หากไม่ส่งให้คงค่าปัจจุบัน
            if (editProfile.getName() != null && !editProfile.getName().isEmpty()) {
                existingUser.setName(editProfile.getName());
            }

            if (editProfile.getLastName() != null && !editProfile.getLastName().isEmpty()) {
                existingUser.setLastName(editProfile.getLastName());
            }

            if (editProfile.getEmail() != null && !editProfile.getEmail().isEmpty()) {
                existingUser.setEmail(editProfile.getEmail());
            }

            if (editProfile.getAddress() != null && !editProfile.getAddress().isEmpty()) {
                existingUser.setAddress(editProfile.getAddress());
            }

            if (editProfile.getTel() != null && !editProfile.getTel().isEmpty()) {
                existingUser.setTel(editProfile.getTel());
            }

            // บันทึกข้อมูลใหม่ลงในฐานข้อมูล
            userRepository.save(existingUser);

            return new ResponseEntity<>("Profile updated successfully.", HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>("Error saving profile image: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ฟังก์ชันสำหรับบีบอัดภาพ
    private byte[] compressImage(byte[] imageBytes) throws IOException {
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));

        if (originalImage == null) {
            throw new IOException("Cannot read the image from the provided byte array");
        }

        // กำหนดขนาดของภาพที่ต้องการ
        int targetWidth = 150; // ความกว้างของภาพโปรไฟล์
        int targetHeight = (int) (originalImage.getHeight() * ((double) targetWidth / originalImage.getWidth()));

        // ลดขนาดภาพ
        Image scaledImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        BufferedImage bufferedScaledImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferedScaledImage.createGraphics();
        g2d.drawImage(scaledImage, 0, 0, null);
        g2d.dispose();

        // แปลงภาพเป็น byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedScaledImage, "jpg", baos);
        baos.flush();
        return baos.toByteArray();
    }
}


