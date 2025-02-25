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
    public AuctionResponse(Tuple tuple) {
        this.auctionId = tuple.get(0, Integer.class);
        this.productName = tuple.get(1, String.class);
        this.description = tuple.get(2, String.class);
        this.startingPrice = tuple.get(3, Double.class);
        this.maxBidPrice = tuple.get(4, Double.class);
        this.imageUrl = tuple.get(7, String.class);

        // 🔥 **ใช้ Timestamp และแปลงเป็น LocalDateTime**
        Timestamp startTimestamp = tuple.get(5, Timestamp.class);
        Timestamp endTimestamp = tuple.get(6, Timestamp.class);
        LocalDateTime startTime = startTimestamp.toLocalDateTime();
        LocalDateTime endTime = endTimestamp.toLocalDateTime();

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