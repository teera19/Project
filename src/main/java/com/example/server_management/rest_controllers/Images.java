package com.example.server_management.rest_controllers;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/images")
public class Images {
    private final String imagesDirectory = "/tmp/images"; //  โฟลเดอร์ที่เก็บรูป

    @GetMapping("/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            //  ป้องกัน Path Traversal Attack (`../`)
            Path filePath = Paths.get(imagesDirectory).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            //  ตรวจสอบ Content-Type ของไฟล์
            String contentType = filename.endsWith(".png") ? "image/png" : "image/jpeg";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    private static final String IMAGE_DIRECTORY = "/tmp/images/";
    @GetMapping("/{auctionId}.jpg")
    public ResponseEntity<Resource> getImage(@PathVariable int auctionId) {
        try {
            Path filePath = Paths.get(IMAGE_DIRECTORY).resolve(auctionId + ".jpg").normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + auctionId + ".jpg\"")
                    .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
