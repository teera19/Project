package com.example.server_management.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class BidHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    private double bidAmount;
    private LocalDateTime bidTime;

    @Column(nullable = false)
    private boolean isWinner;



    // ✅ ต้องมี default constructor
    public BidHistory() {
    }

    // ✅ Constructor ที่ใช้
    public BidHistory(User user, Auction auction, double bidAmount, LocalDateTime bidTime, boolean isWinner) {
        this.user = user;
        this.auction = auction;
        this.bidAmount = bidAmount;
        this.bidTime = bidTime;
        this.isWinner = isWinner;
    }

    // ✅ Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Auction getAuction() { return auction; }
    public void setAuction(Auction auction) { this.auction = auction; }

    public double getBidAmount() { return bidAmount; }
    public void setBidAmount(double bidAmount) { this.bidAmount = bidAmount; }

    public LocalDateTime getBidTime() { return bidTime; }
    public void setBidTime(LocalDateTime bidTime) { this.bidTime = bidTime; }

    public boolean isWinner() { return isWinner; }
    public void setWinner(boolean winner) { isWinner = winner; }
}
