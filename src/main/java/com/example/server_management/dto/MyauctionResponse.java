package com.example.server_management.dto;

import com.example.server_management.models.Auction;
import com.example.server_management.models.BidHistory;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class MyauctionResponse {
    private int auctionId;
    private String productName;
    private String description;
    private double startingPrice;
    private double maxBidPrice;
    private double highestBidPrice; // ✅ ราคาประมูลสูงสุด ณ ปัจจุบัน
    private String startTime;
    private String endTime;
    private String imageUrl;
    private String status;
    private long minutesRemaining;

    public MyauctionResponse(Auction auction, List<BidHistory> bidHistories, String userName) {
        this.auctionId = auction.getAuctionId();
        this.productName = auction.getProductName();
        this.description = auction.getDescription();
        this.startingPrice = auction.getStartingPrice();
        this.maxBidPrice = auction.getMaxBidPrice();
        this.highestBidPrice = bidHistories.stream()
                .mapToDouble(BidHistory::getBidAmount)
                .max()
                .orElse(auction.getStartingPrice()); // ถ้าไม่มีการประมูลใช้ราคาเริ่มต้น

        // ✅ แปลงโซนเวลาเป็น Bangkok
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

        this.startTime = ZonedDateTime.of(auction.getStartTime(), ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Asia/Bangkok"))
                .format(formatter);

        this.endTime = ZonedDateTime.of(auction.getEndTime(), ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Asia/Bangkok"))
                .format(formatter);

        this.imageUrl = auction.getImageUrl();

        // ✅ คำนวณเวลาที่เหลือ
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Bangkok"));
        ZonedDateTime endZoned = ZonedDateTime.of(auction.getEndTime(), ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Asia/Bangkok"));

        if (now.isBefore(endZoned)) {
            this.status = "ONGOING";
            this.minutesRemaining = ChronoUnit.MINUTES.between(now, endZoned);
        } else {
            boolean isWinner = bidHistories.stream()
                    .anyMatch(bid -> bid.getUser().getUserName().equals(userName) && bid.isWinner());

            this.status = isWinner ? "COMPLETED - Won" : "COMPLETED - Lost";
            this.minutesRemaining = 0;
        }
    }

    // ✅ Getters
    public int getAuctionId() { return auctionId; }
    public String getProductName() { return productName; }
    public String getDescription() { return description; }
    public double getStartingPrice() { return startingPrice; }
    public double getMaxBidPrice() { return maxBidPrice; }
    public double getHighestBidPrice() { return highestBidPrice; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getImageUrl() { return imageUrl; }
    public String getStatus() { return status; }
    public long getMinutesRemaining() { return minutesRemaining; }
}
