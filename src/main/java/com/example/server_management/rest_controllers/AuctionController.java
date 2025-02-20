package com.example.server_management.rest_controllers;

import com.example.server_management.dto.AuctionResponse;
import com.example.server_management.dto.BidResponse;
import com.example.server_management.models.*;
import com.example.server_management.repository.BidHistoryRepository;
import com.example.server_management.repository.BidRepository;
import com.example.server_management.repository.UserRepository;
import com.example.server_management.service.AuctionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @GetMapping
    public ResponseEntity<List<AuctionResponse>> getAllAuctions() {
        List<Auction> auctions = auctionService.getAllAuctions();
        List<AuctionResponse> responses = auctions.stream()
                .map(AuctionResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{auctionId}")
    public ResponseEntity<AuctionResponse> getAuctionById(@PathVariable int auctionId) {
        Auction auction = auctionService.getAuctionById(auctionId);
        return ResponseEntity.ok(new AuctionResponse(auction));
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
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

            ZonedDateTime startTime = LocalDateTime.parse(startTimeStr, formatter)
                    .atZone(ZoneId.of("Asia/Bangkok"));

            ZonedDateTime endTime = LocalDateTime.parse(endTimeStr, formatter)
                    .atZone(ZoneId.of("Asia/Bangkok"));

            if (endTime.isBefore(startTime)) {
                return ResponseEntity.badRequest().body(Map.of("message", "End time must be after start time."));
            }

            Auction auction = new Auction();
            auction.setProductName(productName);
            auction.setDescription(description);
            auction.setStartingPrice(startingPrice);
            auction.setMaxBidPrice(maxBidPrice);
            auction.setStartTime(startTime.toLocalDateTime());
            auction.setEndTime(endTime.toLocalDateTime());
            auction.setStatus(AuctionStatus.ONGOING);
            auction.setOwnerUserName(userName);

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

        double bidAmount;
        try {
            bidAmount = Double.parseDouble(bidRequest.get("bidAmount").toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid bid amount."));
        }

        if (bidAmount <= 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Bid amount must be greater than zero."));
        }

        try {
            Bid bid = auctionService.addBid(auctionId, user, bidAmount);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Bid placed successfully!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "An error occurred while processing the bid."));
        }
    }
    @GetMapping("/my-auctions/won")
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
        List<Auction> wonAuctions = auctionService.getWonAuctions(user);

        List<AuctionResponse> responses = wonAuctions.stream()
                .map(AuctionResponse::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

}
