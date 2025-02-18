package com.example.server_management.dto;

import com.example.server_management.models.BidHistory;
import java.time.LocalDateTime;

public class BidResponse {
    private int bidId;               // ID ของการบิด
    private double bidAmount;        // จำนวนเงินที่บิด
    private String username;         // ชื่อผู้ใช้ของผู้ที่บิด
    private String fullName;         // ชื่อ-นามสกุลของผู้ที่บิด
    private LocalDateTime bidTime;   // เวลาที่บิด
    private boolean isWinner;        // สถานะว่าชนะการประมูลหรือไม่

    // ✅ Constructor รับ `BidHistory`
    public BidResponse(BidHistory bidHistory) {
        this.bidId = Math.toIntExact(bidHistory.getId()); // แปลง `Long` เป็น `int` อย่างปลอดภัย
        this.bidAmount = bidHistory.getBidAmount();
        this.username = bidHistory.getUser().getUserName();
        this.fullName = bidHistory.getUser().getName() + " " + bidHistory.getUser().getLastName();
        this.bidTime = bidHistory.getBidTime();
        this.isWinner = bidHistory.isWinner();
    }

    // ✅ Getters
    public int getBidId() { return bidId; }  // แก้ให้เป็น int
    public double getBidAmount() { return bidAmount; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public LocalDateTime getBidTime() { return bidTime; }
    public boolean isWinner() { return isWinner; }
}
