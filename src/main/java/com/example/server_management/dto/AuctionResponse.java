package com.example.server_management.dto;

import com.example.server_management.models.Auction;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class AuctionResponse {
    private int auctionId;
    private String productName;
    private String description;
    private double startingPrice;
    private double maxBidPrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String imageUrl; // ✅ ใช้ URL แทน Base64
    private String status;
    private long minutesRemaining;

    public AuctionResponse(Auction auction) {
        this.auctionId = auction.getAuctionId();
        this.productName = auction.getProductName();
        this.description = auction.getDescription();
        this.startingPrice = auction.getStartingPrice();
        this.maxBidPrice = auction.getMaxBidPrice();
        this.startTime = auction.getStartTime();
        this.endTime = auction.getEndTime();

        // ✅ ดึง URL รูปจาก Cloudinary
        this.imageUrl = auction.getImageUrl();

        // ✅ คำนวณสถานะและเวลาที่เหลือ
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startTime)) {
            this.status = "Not Started";
            this.minutesRemaining = ChronoUnit.MINUTES.between(now, startTime);
        } else if (now.isAfter(endTime)) {
            this.status = "Ended";
            this.minutesRemaining = 0;
        } else {
            this.status = "Active";
            this.minutesRemaining = ChronoUnit.MINUTES.between(now, endTime);
        }
    }

    // ✅ Getters
    public int getAuctionId() { return auctionId; }
    public String getProductName() { return productName; }
    public String getDescription() { return description; }
    public double getStartingPrice() { return startingPrice; }
    public double getMaxBidPrice() { return maxBidPrice; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public String getImageUrl() { return imageUrl; } // ✅ เปลี่ยนเป็น URL
    public String getStatus() { return status; }
    public long getMinutesRemaining() { return minutesRemaining; }
}
