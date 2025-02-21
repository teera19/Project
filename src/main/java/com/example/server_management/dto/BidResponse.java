package com.example.server_management.dto;

import com.example.server_management.models.Bid;
import com.example.server_management.models.BidHistory;
import java.time.LocalDateTime;

public class BidResponse {
    private int bidId;               // ID ของการบิด
    private double bidAmount;        // จำนวนเงินที่บิด
    private String username;         // ชื่อผู้ใช้ของผู้ที่บิด
    private String fullName;         // ชื่อ-นามสกุลของผู้ที่บิด
    private LocalDateTime bidTime;   // เวลาที่บิด

    //  Constructor รับ `Bid`
    public BidResponse(Bid bid) {
        this.bidId = bid.getBidId(); //  ใช้ `id` จาก `Bid`
        this.bidAmount = bid.getBidAmount();
        this.username = bid.getUser().getUserName();
        this.fullName = bid.getUser().getName() + " " + bid.getUser().getLastName();
        this.bidTime = bid.getBidTime();
    }

    //  Getters
    public int getBidId() { return bidId; }
    public double getBidAmount() { return bidAmount; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public LocalDateTime getBidTime() { return bidTime; }
}