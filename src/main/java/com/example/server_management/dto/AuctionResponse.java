package com.example.server_management.dto;

import com.example.server_management.models.Auction;
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
        this.imageUrl = auction.getImageUrl();
        this.maxBidPrice = auction.getMaxBidPrice();
        this.highestBidder = "N/A";  // ตั้งค่าเริ่มต้น
        this.highestBid = auction.getStartingPrice(); // ถ้าไม่มี bid ให้ใช้ราคาตั้งต้น

        setFormattedTimes(auction.getStartTime(), auction.getEndTime());

        // ✅ ถ้าการประมูลมีผู้ชนะแล้ว ให้กำหนดข้อมูล
        if (auction.getWinner() != null) {
            this.highestBidder = auction.getWinner().getUserName();
            this.highestBid = auction.getMaxBidPrice();
            this.status = "Ended";
        }
    }

    public AuctionResponse(Auction auction, BidRepository bidRepository) {
        this.auctionId = auction.getAuctionId();
        this.productName = auction.getProductName();
        this.description = auction.getDescription();
        this.highestBid = auction.getMaxBidPrice(); // ใช้ราคาสูงสุดที่ตั้งไว้
        this.highestBidder = "N/A"; // ค่าเริ่มต้น ถ้ายังไม่มีคนบิด
        this.imageUrl = auction.getImageUrl();

        setFormattedTimes(auction.getStartTime(), auction.getEndTime());
        Bid highestBidObj = bidRepository.findTopByAuctionOrderByBidAmountDesc(auction);
        if (highestBidObj != null) {
            this.highestBid = highestBidObj.getBidAmount();
            this.highestBidder = highestBidObj.getUser().getUserName();
        } else {
            this.highestBid = auction.getStartingPrice();
            this.highestBidder = "N/A";
        }

        // ✅ เช็คว่าใครเป็นผู้ชนะแล้วหรือยัง
        if (auction.getWinner() != null) {
            this.highestBidder = auction.getWinner().getUserName();
            this.highestBid = auction.getMaxBidPrice();
            this.status = "Ended";
        }
    }

    // ✅ Constructor สำหรับแมปจาก Query (Object[])
    public AuctionResponse(Object[] obj) {
        this.auctionId = ((Number) obj[0]).intValue(); // auction_id
        this.productName = (String) obj[1]; // product_name
        this.description = (String) obj[2]; // description
        this.highestBid = obj[3] != null ? ((Number) obj[3]).doubleValue() : 0.0; // ✅ ราคาสูงสุดที่ถูกเสนอ
        this.highestBidder = obj[4] != null ? (String) obj[4] : "No Bids"; // ✅ ชื่อผู้ที่บิดสูงสุด
        this.maxBidPrice = ((Number) obj[5]).doubleValue(); // ✅ เพิ่ม maxBidPrice
        this.imageUrl = (String) obj[8]; // image_url

        Timestamp startTimestamp = (Timestamp) obj[6];
        Timestamp endTimestamp = (Timestamp) obj[7];

        LocalDateTime startTime = startTimestamp != null ? startTimestamp.toLocalDateTime() : null;
        LocalDateTime endTime = endTimestamp != null ? endTimestamp.toLocalDateTime() : null;

        setFormattedTimes(startTime, endTime);

    }



    private void setFormattedTimes(LocalDateTime startTime, LocalDateTime endTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        // ✅ แปลงจาก UTC เป็น Bangkok Time
        this.startTime = ZonedDateTime.of(startTime, ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Asia/Bangkok"))
                .format(formatter);

        this.endTime = ZonedDateTime.of(endTime, ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Asia/Bangkok"))
                .format(formatter);

        // ✅ คำนวณว่าสินค้ากำลังอยู่ในช่วงประมูลหรือไม่
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Bangkok"));
        ZonedDateTime startZoned = ZonedDateTime.parse(this.startTime, formatter.withZone(ZoneId.of("Asia/Bangkok")));
        ZonedDateTime endZoned = ZonedDateTime.parse(this.endTime, formatter.withZone(ZoneId.of("Asia/Bangkok")));

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
    public double getMaxBidPrice() { return maxBidPrice; } // ✅ Getter ใหม่
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getImageUrl() { return imageUrl; }
    public String getStatus() { return status; }
    public long getMinutesRemaining() { return minutesRemaining; }
}