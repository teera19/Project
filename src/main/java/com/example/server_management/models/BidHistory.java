package com.example.server_management.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"auction_id", "isWinner"}))
public class BidHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)  //  โหลดเฉพาะเมื่อเรียกใช้
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore  //  ป้องกันการโหลดข้อมูลซ้ำๆ
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)  //  โหลดเฉพาะเมื่อเรียกใช้
    @JoinColumn(name = "auction_id", nullable = false)
    @JsonIgnore  //  ป้องกันการโหลดข้อมูลซ้ำๆ
    private Auction auction;


    private double bidAmount;
    private LocalDateTime bidTime;

    @Column(nullable = false)
    private boolean isWinner;



    //  ต้องมี default constructor
    public BidHistory() {
    }

    //  Constructor ที่ใช้
    public BidHistory(User user, Auction auction, double bidAmount, LocalDateTime bidTime, boolean isWinner) {
        this.user = user;
        this.auction = auction;
        this.bidAmount = bidAmount;
        this.bidTime = bidTime;
        this.isWinner = isWinner;
    }

    //  Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

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
