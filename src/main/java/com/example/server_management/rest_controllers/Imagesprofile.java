package com.example.server_management.rest_controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/")
public class Imagesprofile {
    private final String imagesDirectory = "uploads";
        @GetMapping(value = "/uploads/{fileName}", produces = MediaType.IMAGE_JPEG_VALUE)
        public ResponseEntity<?> serveProfileImage(@PathVariable String fileName) {
            try {
                Path filePath = Paths.get("uploads/").resolve(fileName).normalize();

                if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
                    return new ResponseEntity<>("File not found", HttpStatus.NOT_FOUND);
                }

                byte[] fileBytes = Files.readAllBytes(filePath);
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(fileBytes);

            } catch (IOException e) {
                return new ResponseEntity<>("Error reading file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }


