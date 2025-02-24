package com.example.server_management.dto;

import com.example.server_management.models.Bid;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class BidResponse {
    private int bidId;
    private double bidAmount;
    private String username;
    private String fullName;
    private String bidTime; //  ใช้ String เพื่อรองรับรูปแบบเวลา

    //  ใช้ Formatter ให้มีความละเอียดระดับเสี้ยววินาที (nanoseconds)
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX");

    public BidResponse(Bid bid) {
        this.bidId = bid.getBidId();
        this.bidAmount = bid.getBidAmount();
        this.username = bid.getUser().getUserName();
        this.fullName = bid.getUser().getName() + " " + bid.getUser().getLastName();

        //  แปลง `bidTime` จาก UTC → Asia/Bangkok และเพิ่ม millisecond
        this.bidTime = ZonedDateTime.of(bid.getBidTime(), ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Asia/Bangkok"))
                .format(formatter);
    }

    //  Getters
    public int getBidId() { return bidId; }
    public double getBidAmount() { return bidAmount; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getBidTime() { return bidTime; } //  คืนค่า bidTime พร้อม milliseconds/nanoseconds
}
