package com.example.server_management.rest_controllers;

import com.example.server_management.dto.AuctionResponse;
import com.example.server_management.dto.BidResponse;
import com.example.server_management.models.Auction;
import com.example.server_management.models.AuctionStatus;
import com.example.server_management.models.Bid;
import com.example.server_management.models.User;
import com.example.server_management.repository.UserRepository;
import com.example.server_management.service.AuctionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auctions")
public class AuctionController {

    @Autowired
    private AuctionService auctionService;
    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<AuctionResponse>> getAllAuctions() {
        List<Auction> auctions = auctionService.getAllAuctions();
        List<AuctionResponse> responses = auctions.stream()
                .map(AuctionResponse::new)
                .collect(Collectors.toList());
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @GetMapping("/{auctionId}") //ดูรายละเอียดสินค้า+สเตตัสว่าปิดรึยัง+เวลา
    public ResponseEntity<AuctionResponse> getAuctionById(@PathVariable int auctionId) {
        Auction auction = auctionService.getAuctionById(auctionId);
        return new ResponseEntity<>(new AuctionResponse(auction), HttpStatus.OK);
    }



    @PostMapping("/add-auction")
    public ResponseEntity<?> addAuction(
            @RequestParam("productName") String productName,
            @RequestParam("description") String description,
            @RequestParam("startingPrice") double startingPrice,
            @RequestParam("maxBidPrice") double maxBidPrice,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam("startTime") String startTimeStr,
            @RequestParam("endTime") String endTimeStr,
            HttpSession session) {

        String userName = (String) session.getAttribute("user_name");
        if (userName == null) {
            return new ResponseEntity<>("User not logged in", HttpStatus.FORBIDDEN);
        }


        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            ZonedDateTime startTime = LocalDateTime.parse(startTimeStr, formatter)
                    .atZone(ZoneId.of("Asia/Bangkok"));
            ZonedDateTime endTime = LocalDateTime.parse(endTimeStr, formatter)
                    .atZone(ZoneId.of("Asia/Bangkok"));

            if (endTime.isBefore(startTime)) {
                return new ResponseEntity<>(Map.of(
                        "message", "End time must be after start time."
                ), HttpStatus.BAD_REQUEST);
            }

            if (maxBidPrice <= startingPrice) {
                return new ResponseEntity<>(Map.of(
                        "message", "Max bid price must be greater than starting price."
                ), HttpStatus.BAD_REQUEST);
            }

            LocalDateTime startTimeLocal = startTime.toLocalDateTime();
            LocalDateTime endTimeLocal = endTime.toLocalDateTime();

            // ✅ แปลงและบีบอัดรูปภาพ
            byte[] compressedImageBytes = null;
            if (image != null && !image.isEmpty()) {
                compressedImageBytes = compressImage(image.getBytes());
            }

            // ✅ สร้าง Auction ใหม่
            Auction auction = new Auction();
            auction.setProductName(productName);
            auction.setDescription(description);
            auction.setStartingPrice(startingPrice);
            auction.setStartTime(startTimeLocal);
            auction.setEndTime(endTimeLocal);
            auction.setImage(compressedImageBytes); // บันทึกเป็น byte[] แทน URL
            auction.setStatus(AuctionStatus.ONGOING);

            Auction savedAuction = auctionService.addAuction(auction);
            return new ResponseEntity<>(savedAuction, HttpStatus.CREATED);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(Map.of(
                    "message", "An error occurred while adding the auction."
            ), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 📌 ฟังก์ชันบีบอัดรูปภาพ
    private byte[] compressImage(byte[] imageBytes) throws IOException {
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageBytes));

        if (originalImage == null) {
            throw new IOException("Cannot read the image from the provided byte array");
        }

        int targetWidth = 100;
        int targetHeight = (int) (originalImage.getHeight() * (100.0 / originalImage.getWidth()));

        Image scaledImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        BufferedImage bufferedScaledImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferedScaledImage.createGraphics();
        g2d.drawImage(scaledImage, 0, 0, null);
        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedScaledImage, "jpg", baos);
        return baos.toByteArray();
    }

    @PostMapping("/{auctionId}/bids") //ประมูล
    public ResponseEntity<?> addBid(@PathVariable int auctionId,
                                    @RequestBody Map<String, Object> bidRequest,
                                    HttpSession session) {
        Integer userId = (Integer) session.getAttribute("user_id");
        if (userId == null) {
            return new ResponseEntity<>(Map.of(
                    "message", "Please log in to participate in the auction."
            ), HttpStatus.FORBIDDEN);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        double bidAmount = Double.parseDouble(bidRequest.get("bidAmount").toString());

        if (bidAmount <= 0) {
            return new ResponseEntity<>(Map.of(
                    "message", "Bid amount must be greater than zero."
            ), HttpStatus.BAD_REQUEST);
        }

        try {
            Auction auction = auctionService.getAuctionById(auctionId);
            LocalDateTime now = LocalDateTime.now();

            // Debugging Logs
            System.out.println("Current server time: " + now);
            System.out.println("Auction start time: " + auction.getStartTime());
            System.out.println("Auction end time: " + auction.getEndTime());

            // ตรวจสอบเวลา
            if (now.isBefore(auction.getStartTime())) {
                return new ResponseEntity<>(Map.of(
                        "message", "Auction has not started yet."
                ), HttpStatus.BAD_REQUEST);
            }
            if (now.isAfter(auction.getEndTime())) {
                return new ResponseEntity<>(Map.of(
                        "message", "Auction has already ended."
                ), HttpStatus.BAD_REQUEST);
            }

            Bid bid = auctionService.addBid(auctionId, user, bidAmount);
            return new ResponseEntity<>(bid, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of(
                    "message", e.getMessage()
            ), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(Map.of(
                    "message", "An error occurred while processing the bid."
            ), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/{auctionId}/bids") //ดูว่าใครประมูล บิดเท่าไหร่
    public ResponseEntity<?> getBidsForAuction(@PathVariable int auctionId) {
        try {
            // ดึงข้อมูล Auction จาก ID
            Auction auction = auctionService.getAuctionById(auctionId);

            // ตรวจสอบว่ามีการประมูลอยู่หรือไม่
            if (auction == null) {
                return new ResponseEntity<>(Map.of(
                        "message", "Auction not found."
                ), HttpStatus.NOT_FOUND);
            }

            // แปลงข้อมูล Bid เป็น Response DTO
            List<BidResponse> bidResponses = auction.getBids().stream()
                    .map(BidResponse::new)
                    .collect(Collectors.toList());

            // ส่งกลับรายการ Bid
            return new ResponseEntity<>(bidResponses, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(Map.of(
                    "message", "An error occurred while fetching bids."
            ), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
