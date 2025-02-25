package com.example.server_management.dto;

import com.example.server_management.models.Auction;

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
    private String highestBidder; // ✅ ผู้ที่บิดราคาสูงสุด
    private double highestBid; // ✅ ราคาสูงสุด ณ ปัจจุบัน
    private double maxBidPrice;
    private String startTime;
    private String endTime;
    private String imageUrl;
    private String status;
    private long minutesRemaining;

    // ✅ Constructor สำหรับแมปจาก Entity `Auction`
    public AuctionResponse(Auction auction) {
        this.auctionId = auction.getAuctionId();
        this.productName = auction.getProductName();
        this.description = auction.getDescription();
        this.highestBid = auction.getMaxBidPrice(); // ใช้ราคาสูงสุดที่ตั้งไว้
        this.highestBidder = "N/A"; // ค่าเริ่มต้น ถ้ายังไม่มีคนบิด
        this.imageUrl = auction.getImageUrl();

        setFormattedTimes(auction.getStartTime(), auction.getEndTime());
    }

    // ✅ Constructor สำหรับแมปจาก Query (Object[])
    public AuctionResponse(Object[] obj) {
        this.auctionId = ((Number) obj[0]).intValue();
        this.productName = (String) obj[1];
        this.description = (String) obj[2];
        this.highestBid = obj[3] != null ? ((Number) obj[3]).doubleValue() : 0.0;
        this.highestBidder = obj[4] != null ? (String) obj[4] : "No Bids";
        this.maxBidPrice = ((Number) obj[5]).doubleValue();
        this.imageUrl = (String) obj[8];

        // ✅ แก้ไขให้รองรับ `Timestamp` และป้องกัน error
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;

        if (obj[6] instanceof Timestamp) {
            startTime = ((Timestamp) obj[6]).toLocalDateTime();
        }
        if (obj[7] instanceof Timestamp) {
            endTime = ((Timestamp) obj[7]).toLocalDateTime();
        }

        setFormattedTimes(startTime, endTime);
    }


    private void setFormattedTimes(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            this.startTime = "N/A";
            this.endTime = "N/A";
            this.status = "Unknown";
            this.minutesRemaining = 0;
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

        this.startTime = ZonedDateTime.of(startTime, ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Asia/Bangkok"))
                .format(formatter);

        this.endTime = ZonedDateTime.of(endTime, ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Asia/Bangkok"))
                .format(formatter);

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Bangkok"));
        ZonedDateTime startZoned = ZonedDateTime.parse(this.startTime);
        ZonedDateTime endZoned = ZonedDateTime.parse(this.endTime);

        if (now.isBefore(startZoned)) {
            this.status = "Not Started"; // 🔹 การประมูลยังไม่เริ่ม
            this.minutesRemaining = ChronoUnit.MINUTES.between(now, startZoned);
        } else if (now.isAfter(endZoned)) {
            this.status = "Ended"; // 🔹 การประมูลจบแล้ว
            this.minutesRemaining = 0;
        } else {
            this.status = "Active"; // 🔹 การประมูลกำลังดำเนินการ
            this.minutesRemaining = ChronoUnit.MINUTES.between(now, endZoned);
        }
    }

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