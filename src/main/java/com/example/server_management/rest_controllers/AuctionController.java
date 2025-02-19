package com.example.server_management.rest_controllers;

import com.example.server_management.dto.AuctionResponse;
import com.example.server_management.dto.BidResponse;
import com.example.server_management.models.*;
import com.example.server_management.repository.BidHistoryRepository;
import com.example.server_management.repository.BidRepository;
import com.example.server_management.repository.UserRepository;
import com.example.server_management.service.AuctionService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController

@RequestMapping("/auctions")
public class AuctionController {

    @Autowired
    private AuctionService auctionService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    BidHistoryRepository bidHistoryRepository;
    @Autowired
    BidRepository bidRepository;


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

        // ✅ ตรวจสอบว่าผู้ใช้ล็อกอินหรือไม่
        String userName = (String) session.getAttribute("user_name");
        if (userName == null) {
            return new ResponseEntity<>(Map.of(
                    "message", "User not logged in"
            ), HttpStatus.FORBIDDEN);
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            ZonedDateTime startTime = LocalDateTime.parse(startTimeStr, formatter).atZone(ZoneId.of("Asia/Bangkok"));
            ZonedDateTime endTime = LocalDateTime.parse(endTimeStr, formatter).atZone(ZoneId.of("Asia/Bangkok"));

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

            byte[] compressedImageBytes = null;
            if (image != null && !image.isEmpty()) {
                compressedImageBytes = compressImage(image.getBytes());
            }

            // ✅ สร้าง Auction และกำหนด ownerUserName
            Auction auction = new Auction();
            auction.setProductName(productName);
            auction.setDescription(description);
            auction.setStartingPrice(startingPrice);
            auction.setMaxBidPrice(maxBidPrice);
            auction.setStartTime(startTimeLocal);
            auction.setEndTime(endTimeLocal);
            auction.setImage(compressedImageBytes);
            auction.setStatus(AuctionStatus.ONGOING);
            auction.setOwnerUserName(userName); // ✅ กำหนด ownerUserName

            Auction savedAuction = auctionService.addAuction(auction);
            return new ResponseEntity<>(savedAuction, HttpStatus.CREATED);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(Map.of(
                    "message", "An error occurred while adding the auction.",
                    "error", e.getMessage()
            ), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(AuctionController.class);

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

    @PostMapping("/{auctionId}/bids")
    public ResponseEntity<?> addBid(@PathVariable int auctionId,
                                    @RequestBody Map<String, Object> bidRequest,
                                    HttpSession session) {
        String userName = (String) session.getAttribute("user_name");
        if (userName == null) {
            return new ResponseEntity<>(Map.of(
                    "message", "Please log in to participate in the auction."
            ), HttpStatus.FORBIDDEN);
        }

        Optional<User> optionalUser = userRepository.findUserByUserName(userName);
        if (!optionalUser.isPresent()) {
            return new ResponseEntity<>(Map.of(
                    "message", "User not found with username: " + userName
            ), HttpStatus.NOT_FOUND);
        }

        User user = optionalUser.get();

        double bidAmount;
        try {
            bidAmount = Double.parseDouble(bidRequest.get("bidAmount").toString());
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of(
                    "message", "Invalid bid amount."
            ), HttpStatus.BAD_REQUEST);
        }

        if (bidAmount <= 0) {
            return new ResponseEntity<>(Map.of(
                    "message", "Bid amount must be greater than zero."
            ), HttpStatus.BAD_REQUEST);
        }

        try {
            Auction auction = auctionService.getAuctionById(auctionId);
            LocalDateTime now = LocalDateTime.now();

            if (now.isBefore(auction.getStartTime())) {
                return new ResponseEntity<>(Map.of(
                        "message", "Auction has not started yet."
                ), HttpStatus.BAD_REQUEST);
            }
            if (now.isAfter(auction.getEndTime()) || auction.getStatus() != AuctionStatus.ONGOING) {
                return new ResponseEntity<>(Map.of(
                        "message", "Auction has already ended."
                ), HttpStatus.BAD_REQUEST);
            }

            Bid bid = auctionService.addBid(auctionId, user, bidAmount);

            // ✅ ตรวจสอบว่ามีการเสนอราคาสูงสุดแล้วหรือไม่
            if (bidAmount >= auction.getMaxBidPrice()) {
                auctionService.determineAuctionWinner(auction);
                return new ResponseEntity<>(Map.of(
                        "message", "Bid placed successfully! You have won the auction.",
                        "winner", user.getUserName()
                ), HttpStatus.OK);
            }

            return new ResponseEntity<>(Map.of("message", "Bid placed successfully!"), HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(Map.of(
                    "message", "An error occurred while processing the bid."
            ), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @GetMapping("/{auctionId}/bids")
    public ResponseEntity<List<BidResponse>> getBidsByAuctionId(@PathVariable int auctionId) {
        List<Bid> bids = bidRepository.findByAuction_AuctionId(auctionId); // ✅ ใช้ BidRepository

        if (bids.isEmpty()) {
            return ResponseEntity.noContent().build(); // 🔥 ไม่มี bid ก็ไม่ต้องส่ง error
        }

        List<BidResponse> responses = bids.stream()
                .map(BidResponse::new) // ✅ ใช้ constructor ที่รับ `Bid`
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }


    @GetMapping("/my-auction")
    public ResponseEntity<?> getMyAuctions(HttpSession session) {
        try {
            String userName = (String) session.getAttribute("user_name");

            if (userName == null) {
                return new ResponseEntity<>(Map.of("message", "User not logged in"), HttpStatus.FORBIDDEN);
            }

            Optional<User> optionalUser = userRepository.findUserByUserName(userName);
            if (!optionalUser.isPresent()) {
                return new ResponseEntity<>(Map.of("message", "User not found with username: " + userName), HttpStatus.NOT_FOUND);
            }

            User user = optionalUser.get();
            List<BidHistory> testBids = bidHistoryRepository.findByUserAndIsWinnerTrue(user);

            if (testBids.isEmpty()) {
                return new ResponseEntity<>(Map.of("message", "No winning auctions found"), HttpStatus.OK);
            }

            // ✅ แก้ไขตรงนี้: เพิ่ม imageUrl ลงไปใน AuctionResponse
            List<AuctionResponse> responses = testBids.stream()
                    .map(bidHistory -> {
                        Auction auction = bidHistory.getAuction();
                        AuctionResponse response = new AuctionResponse(auction);

                        // ✅ ตั้งค่า imageUrl
                        response.setImageUrl("https://project-production-f4db.up.railway.app/images/" + auction.getAuctionId() + ".jpg");

                        return response;
                    })
                    .collect(Collectors.toList());

            return new ResponseEntity<>(responses, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("message", "Internal Server Error", "error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}