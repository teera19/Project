package com.example.server_management.rest_controllers;

import com.example.server_management.models.Auction;
import com.example.server_management.repository.AuctionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@RestController
@RequestMapping("/images")
public class Images {
    @Autowired
    AuctionRepository auctionRepository;
    private final String imagesDirectory = "/tmp/images"; // ✅ โฟลเดอร์ที่เก็บรูป

    @GetMapping("/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            // ✅ ป้องกัน Path Traversal Attack (`../`)
            Path filePath = Paths.get(imagesDirectory).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            // ✅ ตรวจสอบ Content-Type ของไฟล์
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
    @GetMapping("/{auctionId}.jpg")
    public ResponseEntity<byte[]> getAuctionImage(@PathVariable int auctionId) {
        Optional<Auction> auction = auctionRepository.findById(auctionId);

        if (auction.isPresent() && auction.get().getImage() != null) {
            byte[] imageBytes = auction.get().getImage();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
