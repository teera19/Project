package com.example.server_management.rest_controllers;

import com.example.server_management.dto.AuctionResponse;
import com.example.server_management.dto.BidResponse;
import com.example.server_management.models.*;
import com.example.server_management.repository.AuctionRepository;
import com.example.server_management.repository.BidHistoryRepository;
import com.example.server_management.repository.BidRepository;
import com.example.server_management.repository.UserRepository;
import com.example.server_management.service.AuctionService;
import com.example.server_management.service.CloudinaryService;
import jakarta.persistence.Tuple;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
    private BidHistoryRepository bidHistoryRepository;
    @Autowired
    private BidRepository bidRepository;
    @Autowired
    CloudinaryService cloudinaryService;
    @Autowired
    private AuctionRepository auctionRepository;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping
    public ResponseEntity<List<AuctionResponse>> getAllAuctions() {
        List<Auction> auctions = auctionService.getAllAuctions();
        List<AuctionResponse> responses = auctions.stream()
                .map(auction -> new AuctionResponse(auction, bidRepository)) // ✅ ส่ง bidRepository ไปด้วย
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }


    @GetMapping("/{auctionId}")
    public ResponseEntity<AuctionResponse> getAuctionById(@PathVariable int auctionId) {
        Auction auction = auctionService.getAuctionById(auctionId);
        if (auction == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(new AuctionResponse(auction, bidRepository)); // ✅ ใช้ bidRepository
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
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "User not logged in"));
        }

        try {
            // ✅ รับค่าเป็นรูปแบบที่ไม่มีโซนเวลา เช่น "2025-03-01T10:00:00"
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

            // ✅ แปลงจาก LocalDateTime (Bangkok Time) ไปเป็น ZonedDateTime
            LocalDateTime startTimeLocal = LocalDateTime.parse(startTimeStr, formatter);
            LocalDateTime endTimeLocal = LocalDateTime.parse(endTimeStr, formatter);

            // ✅ กำหนดโซนเป็น "Asia/Bangkok"
            ZonedDateTime startTimeBangkok = startTimeLocal.atZone(ZoneId.of("Asia/Bangkok"));
            ZonedDateTime endTimeBangkok = endTimeLocal.atZone(ZoneId.of("Asia/Bangkok"));

            // ✅ แปลงเป็น UTC ก่อนบันทึก
            ZonedDateTime startTimeUTC = startTimeBangkok.withZoneSameInstant(ZoneId.of("UTC"));
            ZonedDateTime endTimeUTC = endTimeBangkok.withZoneSameInstant(ZoneId.of("UTC"));

            if (endTimeUTC.isBefore(startTimeUTC)) {
                return ResponseEntity.badRequest().body(Map.of("message", "End time must be after start time."));
            }

            Auction auction = new Auction();
            auction.setProductName(productName);
            auction.setDescription(description);
            auction.setStartingPrice(startingPrice);
            auction.setMaxBidPrice(maxBidPrice);
            auction.setStartTime(startTimeUTC.toLocalDateTime()); // ✅ บันทึกเป็น UTC
            auction.setEndTime(endTimeUTC.toLocalDateTime());
            auction.setStatus(AuctionStatus.ONGOING);
            auction.setOwnerUserName(userName);

            if (image != null && !image.isEmpty()) {
                String imageUrl = cloudinaryService.uploadImage(image);
                auction.setImageUrl(imageUrl);
            }

            Auction savedAuction = auctionService.addAuction(auction);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedAuction);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "An error occurred while adding the auction.", "error", e.getMessage()));
        }
    }



    @PostMapping("/{auctionId}/bids")
    public ResponseEntity<?> addBid(@PathVariable int auctionId,
                                    @RequestBody Map<String, Object> bidRequest,
                                    HttpSession session) {
        String userName = (String) session.getAttribute("user_name");
        if (userName == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Please log in to participate in the auction."));
        }

        Optional<User> optionalUser = userRepository.findUserByUserName(userName);
        if (!optionalUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found with username: " + userName));
        }

        User user = optionalUser.get();
        double bidAmount = Double.parseDouble(bidRequest.get("bidAmount").toString());

        Auction auction = auctionService.getAuctionById(auctionId);
        if (auction == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Auction not found with ID: " + auctionId));
        }

        // ✅ ค้นหาผู้ที่บิดสูงสุดก่อนหน้านี้
        Bid highestBidObj = bidRepository.findTopByAuctionOrderByBidAmountDesc(auction);
        double highestBid = highestBidObj != null ? highestBidObj.getBidAmount() : auction.getStartingPrice();
        String previousBidder = highestBidObj != null ? highestBidObj.getUser().getUserName() : null;

        // ✅ ตรวจสอบว่าราคาบิดต้องมากกว่าราคาสูงสุดก่อนหน้า
        if (bidAmount <= highestBid) {
            return ResponseEntity.badRequest().body(Map.of("message", "Bid amount must be higher than the current highest bid: " + highestBid));
        }

        // ✅ บันทึก Bid ใหม่
        Bid bid = new Bid();
        bid.setAuction(auction);
        bid.setUser(user);
        bid.setBidAmount(bidAmount);
        bid.setBidTime(ZonedDateTime.now(ZoneId.of("UTC")).toLocalDateTime());
        bidRepository.save(bid);

        // ✅ แจ้งเตือนทุกคนที่ติดตาม Auction นี้
        messagingTemplate.convertAndSend("/topic/auction",
                Map.of("message", "📢 มีคนบิดใหม่ในประมูล " + auction.getProductName() + " ด้วยราคา " + bidAmount));

        // ✅ แจ้งเตือนผู้ที่ถูกแซง
        if (previousBidder != null && !previousBidder.equals(userName)) {
            messagingTemplate.convertAndSendToUser(previousBidder, "/queue/notifications",
                    Map.of("message", "⚠️ คุณถูกบิดแซงในประมูล " + auction.getProductName() + " ด้วยราคา " + bidAmount));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Bid placed successfully!",
                "bidTime", ZonedDateTime.now(ZoneId.of("Asia/Bangkok")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"))
        ));
    }

    @GetMapping("/{auctionId}/bids")
    public ResponseEntity<?> getBidsForAuction(@PathVariable int auctionId) {
        Auction auction = auctionService.getAuctionById(auctionId);
        if (auction == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Auction not found with ID: " + auctionId));
        }

        List<Bid> bids = bidRepository.findByAuction(auction);
        if (bids.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "No bids found for this auction."));
        }

        // ✅ แปลง `BidResponse` ให้แสดงเวลาเป็น Bangkok Time พร้อม Milliseconds
        List<BidResponse> bidResponses = bids.stream()
                .map(BidResponse::new) // 🔥 แปลงเวลา UTC → Bangkok
                .collect(Collectors.toList());

        return ResponseEntity.ok(bidResponses);
    }


    @GetMapping("/my-auction")
    public ResponseEntity<?> getMyBidAuctions(HttpSession session) {
        String userName = (String) session.getAttribute("user_name");
        if (userName == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Please log in to view your auctions."));
        }

        Optional<User> optionalUser = userRepository.findUserByUserName(userName);
        if (!optionalUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found with username: " + userName));
        }

        User user = optionalUser.get();

        System.out.println("🔍 Fetching participated auctions for user: " + user.getUserName());
        List<Object[]> auctionData = bidRepository.findAllParticipatedAuctions(user.getUserId());
        System.out.println("✅ Total Auctions Retrieved: " + auctionData.size());

        // ✅ AuctionResponse จะแปลงเวลาเป็น Bangkok ให้อัตโนมัติ
        List<AuctionResponse> responses = auctionData.stream()
                .map(data -> new AuctionResponse(data)) // ✅ ใช้ Lambda แทน Method Reference
                .toList();


        return ResponseEntity.ok(responses);
    }
    @GetMapping("/my-auctions")
    public ResponseEntity<?> getMyAuctions(HttpSession session) {
        String userName = (String) session.getAttribute("user_name");
        if (userName == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Please log in to view your own auctions."));
        }

        List<Auction> myAuctions = auctionRepository.findByOwnerUserName(userName);

        if (myAuctions.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "You have not listed any auctions."));
        }

        // ✅ ใช้ AuctionResponse ที่แปลงเวลาจาก UTC → Bangkok ให้แล้ว
        List<AuctionResponse> responses = myAuctions.stream()
                .map(auction -> new AuctionResponse(auction, bidRepository))
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }
    @DeleteMapping("/my-auctions/{auctionId}")
    public ResponseEntity<?> deleteMyAuction(@PathVariable int auctionId, HttpSession session) {
        String userName = (String) session.getAttribute("user_name");
        if (userName == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Please log in to delete your auction."));
        }

        // 🔍 ค้นหา Auction
        Optional<Auction> optionalAuction = auctionRepository.findById(auctionId);
        if (!optionalAuction.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Auction not found with ID: " + auctionId));
        }

        Auction auction = optionalAuction.get();

        // ❌ เช็คว่าเป็นเจ้าของไหม?
        if (!auction.getOwnerUserName().equals(userName)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You are not the owner of this auction."));
        }

        // ❌ เช็คว่า Auction จบแล้วหรือยัง?
        if (auction.getStatus() != AuctionStatus.COMPLETED) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You can only delete an auction that has ended."));
        }

        // 🔥 ลบประมูล
        auctionRepository.delete(auction);
        return ResponseEntity.ok(Map.of("message", "Auction deleted successfully!"));
    }
    @DeleteMapping("/my-auction/{auctionId}")
    public ResponseEntity<?> removeMyBid(@PathVariable int auctionId, HttpSession session) {
        String userName = (String) session.getAttribute("user_name");
        if (userName == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Please log in to remove your bid."));
        }

        Optional<User> optionalUser = userRepository.findUserByUserName(userName);
        if (!optionalUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found with username: " + userName));
        }

        User user = optionalUser.get();
        Optional<Auction> optionalAuction = auctionRepository.findById(auctionId);

        if (!optionalAuction.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Auction not found with ID: " + auctionId));
        }

        Auction auction = optionalAuction.get();

        // ❌ ตรวจสอบว่า Auction จบแล้วหรือยัง?
        if (auction.getStatus() != AuctionStatus.COMPLETED) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "You can only remove your bid after the auction has ended."));
        }

        // ❌ ลบเฉพาะ Bid ของตัวเองเท่านั้น
        List<Bid> userBids = bidRepository.findByAuctionAndUser(auction, user);
        if (userBids.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No bids found for this auction by you."));
        }

        bidRepository.deleteAll(userBids);
        return ResponseEntity.ok(Map.of("message", "Your bid has been removed from the auction."));
    }
    @GetMapping("/my-auctionwin")
    public ResponseEntity<?> getMyWonAuctions(HttpSession session) {
        String userName = (String) session.getAttribute("user_name");
        if (userName == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Please log in to view your won auctions."));
        }

        Optional<User> optionalUser = userRepository.findUserByUserName(userName);
        if (!optionalUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found with username: " + userName));
        }

        User user = optionalUser.get();

        // 🔍 ค้นหา Auction ที่ผู้ใช้เป็นผู้ชนะ
        List<Auction> wonAuctions = auctionRepository.findByWinner(user);

        if (wonAuctions.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "You have not won any auctions."));
        }

        // ✅ ใช้ `AuctionResponse` และส่ง `bidRepository` ไปด้วย
        List<AuctionResponse> responses = wonAuctions.stream()
                .map(auction -> new AuctionResponse(auction, bidRepository))
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

}