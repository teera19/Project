package com.example.server_management.dto;

import com.example.server_management.models.Auction;
import com.example.server_management.models.AuctionStatus;
import com.example.server_management.models.Bid;
import com.example.server_management.repository.BidRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;

public class AuctionResponse {
    private int auctionId;
    private String productName;
    private String description;
    private String highestBidder;
    private double highestBid;
    private double maxBidPrice;
    private String startTime;
    private String endTime;
    private String imageUrl;
    private String status;
    private long minutesRemaining;
    private long secondsRemaining;

    // ✅ Constructor สำหรับแมปจาก Entity `Auction`
    public AuctionResponse(Auction auction, BidRepository bidRepository) {
        this.auctionId = auction.getAuctionId();
        this.productName = auction.getProductName();
        this.description = auction.getDescription();
        this.imageUrl = auction.getImageUrl();
        this.maxBidPrice = auction.getMaxBidPrice(); // ✅ ราคาสูงสุดที่ตั้งไว้

        setFormattedTimes(auction.getStartTime(), auction.getEndTime());

        // ✅ ดึงข้อมูลคนที่บิดราคาสูงสุด
        Bid highestBidObj = bidRepository.findTopByAuctionOrderByBidAmountDesc(auction);
        if (highestBidObj != null) {
            this.highestBid = highestBidObj.getBidAmount();
            this.highestBidder = highestBidObj.getUser().getUserName();
        } else {
            this.highestBid = auction.getStartingPrice(); // ถ้าไม่มีคนบิด ใช้ราคาตั้งต้น
            this.highestBidder = "No Bids";
        }

        // ✅ ถ้าการประมูลสิ้นสุดแล้ว ให้ใช้ราคาของผู้ชนะจริงๆ
        if (auction.getStatus() == AuctionStatus.ENDED && auction.getWinner() != null) {
            this.highestBidder = auction.getWinner().getUserName();
            this.highestBid = bidRepository.findTopByAuctionOrderByBidAmountDesc(auction).getBidAmount(); // ✅ ใช้ราคาสูงสุดของผู้ชนะจริงๆ
            this.status = "Ended";
        }
    }

    public AuctionResponse(Object[] data) {
        this.auctionId = ((Number) data[0]).intValue(); // auction_id
        this.productName = (String) data[1]; // product_name
        this.description = (String) data[2]; // description
        this.highestBid = data[3] != null ? ((Number) data[3]).doubleValue() : 0.0; // ราคาสูงสุด
        this.highestBidder = data[4] != null ? (String) data[4] : "No Bids"; // ชื่อผู้บิดสูงสุด
        this.maxBidPrice = ((Number) data[5]).doubleValue(); // maxBidPrice
        this.imageUrl = (String) data[8]; // image_url

        Timestamp startTimestamp = (Timestamp) data[6];
        Timestamp endTimestamp = (Timestamp) data[7];

        LocalDateTime startTime = startTimestamp != null ? startTimestamp.toLocalDateTime() : null;
        LocalDateTime endTime = endTimestamp != null ? endTimestamp.toLocalDateTime() : null;

        setFormattedTimes(startTime, endTime);
    }


    private void setFormattedTimes(LocalDateTime startTime, LocalDateTime endTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        // ✅ แปลงจาก UTC -> Bangkok
        ZonedDateTime startZoned = ZonedDateTime.of(startTime, ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Asia/Bangkok"));
        ZonedDateTime endZoned = ZonedDateTime.of(endTime, ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Asia/Bangkok"));

        // ✅ แปลงเป็น String ที่เข้าใจง่าย
        this.startTime = startZoned.format(formatter);
        this.endTime = endZoned.format(formatter);

        // ✅ ตรวจสอบสถานะ
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Bangkok"));

        System.out.println("🔍 Debug: Checking auction status");
        System.out.println("   - Now (Bangkok): " + now);
        System.out.println("   - Start Time (Bangkok): " + startZoned);
        System.out.println("   - End Time (Bangkok): " + endZoned);

        if (now.isBefore(startZoned)) {
            this.status = "Not Started";
            this.minutesRemaining = ChronoUnit.MINUTES.between(now, startZoned);
            this.secondsRemaining = ChronoUnit.SECONDS.between(now, startZoned) % 60; // ✅ แสดงวินาที
        } else if (now.isBefore(endZoned)) { // ✅ ถ้ายังไม่หมดเวลา
            this.status = "Active"; // ✅ ตอนนี้ต้องเป็น "Active"
            this.minutesRemaining = ChronoUnit.MINUTES.between(now, endZoned);
            this.secondsRemaining = ChronoUnit.SECONDS.between(now, endZoned) % 60; // ✅ แสดงวินาที
        } else {
            this.status = "Ended";
            this.minutesRemaining = 0;
            this.secondsRemaining = 0;
        }
    }

    // ✅ เพิ่ม Getter
    public long getSecondsRemaining() { return secondsRemaining; }


    // ✅ Getters
    public int getAuctionId() { return auctionId; }
    public String getProductName() { return productName; }
    public String getDescription() { return description; }
    public String getHighestBidder() { return highestBidder; }
    public double getHighestBid() { return highestBid; }
    public double getMaxBidPrice() { return maxBidPrice; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getImageUrl() { return imageUrl; }
    public String getStatus() { return status; }
    public long getMinutesRemaining() { return minutesRemaining; }
}
