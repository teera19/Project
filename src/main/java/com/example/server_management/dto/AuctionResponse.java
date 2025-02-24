package com.example.server_management.dto;

import com.example.server_management.models.Auction;
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

    public AuctionResponse(Auction auction) {
        this.auctionId = auction.getAuctionId();
        this.productName = auction.getProductName();
        this.description = auction.getDescription();
        this.startingPrice = auction.getStartingPrice();
        this.maxBidPrice = auction.getMaxBidPrice();

        // ✅ ใช้ ZoneId เพื่อแปลง UTC → Asia/Bangkok
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

        this.startTime = ZonedDateTime.of(auction.getStartTime(), ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Asia/Bangkok"))
                .format(formatter);

        this.endTime = ZonedDateTime.of(auction.getEndTime(), ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Asia/Bangkok"))
                .format(formatter);

        this.imageUrl = auction.getImageUrl();

        // ✅ คำนวณเวลาที่เหลือโดยใช้โซนเวลา Bangkok
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Bangkok"));

        ZonedDateTime startZoned = ZonedDateTime.of(auction.getStartTime(), ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Asia/Bangkok"));
        ZonedDateTime endZoned = ZonedDateTime.of(auction.getEndTime(), ZoneId.of("UTC"))
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
    public String getStartTime() { return startTime; } // ✅ ส่งค่าเป็น Bangkok
    public String getEndTime() { return endTime; } // ✅ ส่งค่าเป็น Bangkok
    public String getImageUrl() { return imageUrl; }
    public String getStatus() { return status; }
    public long getMinutesRemaining() { return minutesRemaining; }
}
