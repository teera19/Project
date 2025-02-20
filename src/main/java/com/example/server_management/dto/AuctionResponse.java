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
    private String imageUrl;
    private String status;
    private long minutesRemaining;

    // ✅ Constructor รับ Object Auction โดยตรง
    public AuctionResponse(Auction auction) {
        this.auctionId = auction.getAuctionId();
        this.productName = auction.getProductName();
        this.description = auction.getDescription();
        this.startingPrice = auction.getStartingPrice();
        this.maxBidPrice = auction.getMaxBidPrice();
        this.startTime = auction.getStartTime();
        this.endTime = auction.getEndTime();
        this.imageUrl = (auction.getImageUrl() != null && !auction.getImageUrl().isEmpty())
                ? auction.getImageUrl()
                : "https://project-production-f4db.up.railway.app/images/default.jpg";
        this.status = auction.getStatus().name(); // ✅ Enum -> String

        // ✅ คำนวณเวลาที่เหลือ
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startTime)) {
            this.minutesRemaining = ChronoUnit.MINUTES.between(now, startTime);
        } else if (now.isAfter(endTime)) {
            this.minutesRemaining = 0;
        } else {
            this.minutesRemaining = ChronoUnit.MINUTES.between(now, endTime);
        }
    }

    // ✅ Constructor รองรับค่าทีละตัว
    public AuctionResponse(int auctionId, String productName, String description,
                           double startingPrice, double maxBidPrice,
                           LocalDateTime startTime, LocalDateTime endTime,
                           String imageUrl, String status) {
        this.auctionId = auctionId;
        this.productName = productName;
        this.description = description;
        this.startingPrice = startingPrice;
        this.maxBidPrice = maxBidPrice;
        this.startTime = startTime;
        this.endTime = endTime;
        this.imageUrl = (imageUrl != null && !imageUrl.isEmpty())
                ? imageUrl
                : "https://project-production-f4db.up.railway.app/images/default.jpg";
        this.status = status;

        // ✅ คำนวณเวลาที่เหลือ
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startTime)) {
            this.minutesRemaining = ChronoUnit.MINUTES.between(now, startTime);
        } else if (now.isAfter(endTime)) {
            this.minutesRemaining = 0;
        } else {
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
    public String getImageUrl() { return imageUrl; }
    public String getStatus() { return status; }
    public long getMinutesRemaining() { return minutesRemaining; }
}
