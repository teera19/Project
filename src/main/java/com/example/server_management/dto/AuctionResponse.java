package com.example.server_management.dto;

import com.example.server_management.models.Auction;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

public class AuctionResponse {
    private int auctionId;
    private String productName;
    private String description;
    private double startingPrice;
    private double maxBidPrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String imageBase64; // ✅ เปลี่ยนเป็น Base64
    private String status; // ✅ สถานะ เช่น "Not Started", "Active", "Ended"
    private long minutesRemaining; // ✅ นาทีที่เหลือ

    public AuctionResponse(Auction auction) {
        this.auctionId = auction.getAuctionId();
        this.productName = auction.getProductName();
        this.description = auction.getDescription();
        this.startingPrice = auction.getStartingPrice();
        this.maxBidPrice = auction.getMaxBidPrice();
        this.startTime = auction.getStartTime();
        this.endTime = auction.getEndTime();

        // ✅ แปลง byte[] เป็น Base64 เพื่อให้ frontend ใช้งานได้ง่าย
        if (auction.getImage() != null) {
            String fullBase64 = Base64.getEncoder().encodeToString(auction.getImage());
            this.imageBase64 = fullBase64.substring(0, Math.min(fullBase64.length(), 100));
        } else {
            this.imageBase64 = null;
        }

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
    public int getAuctionId() {
        return auctionId;
    }

    public String getProductName() {
        return productName;
    }

    public String getDescription() {
        return description;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public double getMaxBidPrice() {
        return maxBidPrice;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getImageBase64() { // ✅ ใช้ชื่อใหม่
        return imageBase64;
    }

    public String getStatus() {
        return status;
    }

    public long getMinutesRemaining() {
        return minutesRemaining;
    }
}