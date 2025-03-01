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

    // ✅ Constructor สำหรับแมปจาก Entity `Auction`
    public AuctionResponse(Auction auction, BidRepository bidRepository) {
        this.auctionId = auction.getAuctionId();
        this.productName = auction.getProductName();
        this.description = auction.getDescription();
        this.imageUrl = auction.getImageUrl();
        this.maxBidPrice = auction.getMaxBidPrice();
        this.highestBidder = "N/A"; // ค่าเริ่มต้น
        this.highestBid = auction.getStartingPrice();

        setFormattedTimes(auction.getStartTime(), auction.getEndTime());

        // ✅ ดึงข้อมูลผู้ที่บิดราคาสูงสุด
        Bid highestBidObj = bidRepository.findTopByAuctionOrderByBidAmountDesc(auction);
        if (highestBidObj != null) {
            this.highestBid = highestBidObj.getBidAmount();
            this.highestBidder = highestBidObj.getUser().getUserName();
        }

        // ✅ ถ้าการประมูลมีผู้ชนะแล้ว ให้กำหนดข้อมูล
        if (auction.getStatus() == AuctionStatus.ENDED && auction.getWinner() != null) {
            this.highestBidder = auction.getWinner().getUserName(); // ✅ ใช้ getUserName() ตรงๆ
            this.highestBid = auction.getMaxBidPrice();
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

        this.startTime = ZonedDateTime.of(startTime, ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Asia/Bangkok"))
                .format(formatter);

        this.endTime = ZonedDateTime.of(endTime, ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Asia/Bangkok"))
                .format(formatter);

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Bangkok"));
        ZonedDateTime startZoned = ZonedDateTime.parse(this.startTime, formatter.withZone(ZoneId.of("Asia/Bangkok")));
        ZonedDateTime endZoned = ZonedDateTime.parse(this.endTime, formatter.withZone(ZoneId.of("Asia/Bangkok")));

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
    public String getHighestBidder() { return highestBidder; }
    public double getHighestBid() { return highestBid; }
    public double getMaxBidPrice() { return maxBidPrice; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getImageUrl() { return imageUrl; }
    public String getStatus() { return status; }
    public long getMinutesRemaining() { return minutesRemaining; }
}
