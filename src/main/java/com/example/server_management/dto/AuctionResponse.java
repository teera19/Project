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
        this.auctionId = ((Number) obj[0]).intValue(); // auction_id
        this.productName = (String) obj[1]; // product_name
        this.description = (String) obj[2]; // description
        this.highestBid = obj[3] != null ? ((Number) obj[3]).doubleValue() : 0.0; // ✅ ราคาสูงสุด
        this.highestBidder = obj[4] != null ? (String) obj[4] : "No Bids"; // ✅ ชื่อผู้ที่บิดสูงสุด
        this.imageUrl = (String) obj[7]; // image_url

        // ✅ ตรวจสอบ timestamp และแปลงเป็น LocalDateTime
        Timestamp startTimestamp = (Timestamp) obj[5];
        Timestamp endTimestamp = (Timestamp) obj[6];

        LocalDateTime startTime = startTimestamp != null ? startTimestamp.toLocalDateTime() : null;
        LocalDateTime endTime = endTimestamp != null ? endTimestamp.toLocalDateTime() : null;

        setFormattedTimes(startTime, endTime);
    }

    private void setFormattedTimes(LocalDateTime startTime, LocalDateTime endTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

        this.startTime = ZonedDateTime.of(startTime, ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Asia/Bangkok"))
                .format(formatter);

        this.endTime = ZonedDateTime.of(endTime, ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Asia/Bangkok"))
                .format(formatter);

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Bangkok"));
        ZonedDateTime endZoned = ZonedDateTime.parse(this.endTime);

        this.minutesRemaining = now.isBefore(endZoned) ? ChronoUnit.MINUTES.between(now, endZoned) : 0;
    }

    // ✅ Getters
    public int getAuctionId() { return auctionId; }
    public String getProductName() { return productName; }
    public String getDescription() { return description; }
    public String getHighestBidder() { return highestBidder; }
    public double getHighestBid() { return highestBid; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getImageUrl() { return imageUrl; }
    public String getStatus() { return status; }
    public long getMinutesRemaining() { return minutesRemaining; }
}
