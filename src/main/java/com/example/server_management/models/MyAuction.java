package com.example.server_management.models;

import jakarta.persistence.*;

@Entity
@Table(name = "my_auctions")
public class MyAuction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    @JoinColumn(name = "auction_id", nullable = false, unique = true)
    private Auction auction;

    @ManyToOne
    @JoinColumn(name = "winner_id", nullable = false)
    private User winner;

    public MyAuction() {}

    public MyAuction(Auction auction, User winner) {
        this.auction = auction;
        this.winner = winner;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Auction getAuction() {
        return auction;
    }

    public void setAuction(Auction auction) {
        this.auction = auction;
    }

    public User getWinner() {
        return winner;
    }

    public void setWinner(User winner) {
        this.winner = winner;
    }
}
