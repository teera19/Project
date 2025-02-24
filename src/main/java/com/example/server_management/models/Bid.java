package com.example.server_management.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table(name = "bid")
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bid_id")
    private int bidId;

    @ManyToOne
    @JoinColumn(name = "auction_id", nullable = false)
    @JsonIgnore // ป้องกันการวนลูป JSON
    private Auction auction;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "bid_amount", nullable = false)
    private double bidAmount;

    //  รองรับ millisecond/nanosecond
    @Column(name = "bid_time", nullable = false, updatable = false, columnDefinition = "TIMESTAMP(6)")
    private LocalDateTime bidTime;

    @PrePersist
    protected void onCreate() {
        // บันทึก bidTime เป็น UTC เพื่อให้จัดการเวลาได้ง่ายขึ้น
        this.bidTime = ZonedDateTime.now(ZoneId.of("Asia/Bangkok"))
                .withZoneSameInstant(ZoneId.of("UTC"))
                .toLocalDateTime();
    }

    //  Getters และ Setters
    public int getBidId() {
        return bidId;
    }

    public void setBidId(int bidId) {
        this.bidId = bidId;
    }

    public Auction getAuction() {
        return auction;
    }

    public void setAuction(Auction auction) {
        this.auction = auction;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public double getBidAmount() {
        return bidAmount;
    }

    public void setBidAmount(double bidAmount) {
        this.bidAmount = bidAmount;
    }

    public LocalDateTime getBidTime() {
        return bidTime;
    }

    public void setBidTime(LocalDateTime bidTime) {
        this.bidTime = bidTime;
    }
}
