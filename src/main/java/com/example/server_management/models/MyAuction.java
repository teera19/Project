package com.example.server_management.models;


import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "my_auction")
public class MyAuction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id", nullable = false) // อ้างอิงผู้ชนะประมูล
    private User winner;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false) // อ้างอิง Auction
    private Auction auction;

    private LocalDateTime wonTime; // เวลาที่ชนะการประมูล

    // Constructor
    public MyAuction() {}

    public MyAuction(User winner, Auction auction, LocalDateTime wonTime) {
        this.winner = winner;
        this.auction = auction;
        this.wonTime = wonTime;
    }

    // Getter & Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getWinner() {
        return winner;
    }

    public void setWinner(User winner) {
        this.winner = winner;
    }

    public Auction getAuction() {
        return auction;
    }

    public void setAuction(Auction auction) {
        this.auction = auction;
    }

    public LocalDateTime getWonTime() {
        return wonTime;
    }

    public void setWonTime(LocalDateTime wonTime) {
        this.wonTime = wonTime;
    }
}
