package com.example.server_management.dto;

import com.example.server_management.models.Bid;
import java.time.ZonedDateTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class BidResponse {
    private int bidId;
    private double bidAmount;
    private String username;
    private String fullName;
    private String bidTime; // ✅ เปลี่ยนเป็น String เพื่อแสดงค่า +07:00

    // ✅ ใช้ Formatter สำหรับแสดงค่าเวลาที่ชัดเจน
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    public BidResponse(Bid bid) {
        this.bidId = bid.getBidId();
        this.bidAmount = bid.getBidAmount();
        this.username = bid.getUser().getUserName();
        this.fullName = bid.getUser().getName() + " " + bid.getUser().getLastName();

        // ✅ แปลง `bidTime` จาก UTC → Asia/Bangkok
        this.bidTime = ZonedDateTime.of(bid.getBidTime(), ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Asia/Bangkok"))
                .format(formatter);
    }

    // ✅ Getters
    public int getBidId() { return bidId; }
    public double getBidAmount() { return bidAmount; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getBidTime() { return bidTime; } // ✅ ส่งเวลาเป็น Bangkok (+07:00)
}
