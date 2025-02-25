package com.example.server_management.dto;

import com.example.server_management.models.Auction;
import jakarta.persistence.Tuple;

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
    private double startingPrice;
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
        this.startingPrice = auction.getStartingPrice();
        this.maxBidPrice = auction.getMaxBidPrice();
        this.imageUrl = auction.getImageUrl();

        setFormattedTimes(auction.getStartTime(), auction.getEndTime());
    }

    // ✅ Constructor สำหรับแมปจาก `Tuple` (ใช้สำหรับ Native Query)
    public AuctionResponse(Object[] obj) {
        this.auctionId = ((Number) obj[0]).intValue(); // auction_id
        this.productName = (String) obj[1]; // product_name
        this.description = (String) obj[2]; // description
        this.startingPrice = ((Number) obj[3]).doubleValue(); // starting_price
        this.maxBidPrice = ((Number) obj[4]).doubleValue(); // max_bid_price
        this.imageUrl = (String) obj[7]; // image_url

        // ✅ ตรวจสอบ timestamp และแปลงเป็น LocalDateTime
        Timestamp startTimestamp = (Timestamp) obj[5];
        Timestamp endTimestamp = (Timestamp) obj[6];

        LocalDateTime startTime = startTimestamp != null ? startTimestamp.toLocalDateTime() : null;
        LocalDateTime endTime = endTimestamp != null ? endTimestamp.toLocalDateTime() : null;

        setFormattedTimes(startTime, endTime);
    }


    private void setFormattedTimes(LocalDateTime startTime, LocalDateTime endTime) {
        // ✅ ใช้ ZoneId เพื่อแปลง UTC → Asia/Bangkok
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

        this.startTime = ZonedDateTime.of(startTime, ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Asia/Bangkok"))
                .format(formatter);

        this.endTime = ZonedDateTime.of(endTime, ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Asia/Bangkok"))
                .format(formatter);

        // ✅ คำนวณเวลาที่เหลือโดยใช้โซนเวลา Bangkok
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Bangkok"));
        ZonedDateTime startZoned = ZonedDateTime.of(startTime, ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Asia/Bangkok"));
        ZonedDateTime endZoned = ZonedDateTime.of(endTime, ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Asia/Bangkok"));

        if (now.isBefore(startZoned)) {
            this.status = "Not Started";
            this.minutesRemaining = ChronoUnit.MINUTES.between(now, startZoned);
        } else if (now.isAfter(endZoned)) {
            this.status = "Ended";
            this.minutesRemaining = 0;
        } else {
            this.status = "Active";
            this.minutesRemaining = ChronoUnit.MINUTES.between(now, endZoned);
        }
    }

    // ✅ Getters
    public int getAuctionId() { return auctionId; }
    public String getProductName() { return productName; }
    public String getDescription() { return description; }
    public double getStartingPrice() { return startingPrice; }
    public double getMaxBidPrice() { return maxBidPrice; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getImageUrl() { return imageUrl; }
    public String getStatus() { return status; }
    public long getMinutesRemaining() { return minutesRemaining; }
}