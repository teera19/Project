package com.example.server_management.rest_controllers;

import com.example.server_management.dto.AuctionResponse;
import com.example.server_management.dto.AuctionSummaryResponse;
import com.example.server_management.dto.BidResponse;
import com.example.server_management.models.*;
import com.example.server_management.repository.AuctionRepository;
import com.example.server_management.repository.BidHistoryRepository;
import com.example.server_management.repository.BidRepository;
import com.example.server_management.repository.UserRepository;
import com.example.server_management.service.AuctionService;
import jakarta.servlet.http.HttpSession;
import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    @Autowired
    private AuctionRepository auctionRepository;


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
            return new ResponseEntity<>(Map.of("message", "User not logged in"), HttpStatus.FORBIDDEN);
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

            //  แปลงค่าเวลาจาก String โดยให้ใช้โซนเวลา `Asia/Bangkok`
            LocalDateTime startTime = LocalDateTime.parse(startTimeStr, formatter);
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr, formatter);

            System.out.println(" Received Start Time: " + startTime);
            System.out.println(" Received End Time: " + endTime);

            //  ตรวจสอบเวลาการประมูล
            if (endTime.isBefore(startTime)) {
                return new ResponseEntity<>(Map.of("message", "End time must be after start time."), HttpStatus.BAD_REQUEST);
            }
            if (maxBidPrice <= startingPrice) {
                return new ResponseEntity<>(Map.of("message", "Max bid price must be greater than starting price."), HttpStatus.BAD_REQUEST);
            }

            //  สร้าง Object Auction ก่อน
            Auction auction = new Auction();
            auction.setProductName(productName);
            auction.setDescription(description);
            auction.setStartingPrice(startingPrice);
            auction.setMaxBidPrice(maxBidPrice);
            auction.setStartTime(startTime);
            auction.setEndTime(endTime);
            auction.setOwnerUserName(userName);
            auction.setStatus(AuctionStatus.ONGOING);

            //  บันทึก Auction ลงฐานข้อมูลก่อน เพื่อให้มี `auctionId`
            Auction savedAuction = auctionService.addAuction(auction);

            //  บันทึกภาพถ้ามี
            if (image != null && !image.isEmpty()) {
                try {
                    String imageUrl = saveImageToFile(image, savedAuction.getAuctionId());
                    savedAuction.setImageUrl(imageUrl);
                    auctionService.updateAuctionStatus(savedAuction); //  บันทึก URL ลง database
                } catch (IOException e) {
                    return new ResponseEntity<>(Map.of(
                            "message", "Failed to save image",
                            "error", e.getMessage()
                    ), HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }

            return new ResponseEntity<>(savedAuction, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("message", "An error occurred", "error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    private static final Logger log = LoggerFactory.getLogger(AuctionController.class);

    //  ฟังก์ชันบีบอัดรูปภาพ
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

            //  ตรวจสอบว่ามีการเสนอราคาสูงสุดแล้วหรือไม่
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
        List<Bid> bids = bidRepository.findByAuction_AuctionId(auctionId); // ใช้ BidRepository

        if (bids.isEmpty()) {
            return ResponseEntity.noContent().build(); // 🔥 ไม่มี bid ก็ไม่ต้องส่ง error
        }

        List<BidResponse> responses = bids.stream()
                .map(BidResponse::new) //  ใช้ constructor ที่รับ `Bid`
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
                return new ResponseEntity<>(Map.of("message", "User not found"), HttpStatus.NOT_FOUND);
            }

            User user = optionalUser.get();
            List<Auction> auctions = auctionRepository.findByWinner(user);

            if (auctions.isEmpty()) {
                return new ResponseEntity<>(Map.of("message", "No winning auctions found"), HttpStatus.OK);
            }

            // ✅ แปลง `Auction` เป็น `AuctionSummaryResponse`
            List<AuctionSummaryResponse> responses = auctions.stream()
                    .map(AuctionSummaryResponse::new)
                    .collect(Collectors.toList());

            return new ResponseEntity<>(Map.of("auctions", responses), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("message", "Internal Server Error", "error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    private String saveImageToFile(MultipartFile image, int auctionId) throws IOException {
        File uploadDir = new File("/tmp/images/");
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        String fileName = auctionId + ".jpg";
        File savedFile = new File(uploadDir, fileName);

        // ✅ บีบอัดรูปก่อนบันทึก
        Thumbnails.of(image.getInputStream())
                .size(500, 500) // ลดขนาดรูปเป็น 500x500
                .outputQuality(0.7) // ลดคุณภาพรูปให้ขนาดไฟล์เล็กลง
                .toFile(savedFile);

        System.out.println("✅ Compressed Image saved successfully: " + savedFile.getAbsolutePath());

        return "https://project-production-f4db.up.railway.app/images/" + fileName; // ✅ ส่ง URL ของรูปที่ Railway ให้บริการ
    }
}