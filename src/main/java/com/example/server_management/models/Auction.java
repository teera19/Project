package com.example.server_management.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Entity
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int auctionId;

    private String productName;
    private String description;
    private double startingPrice;
    private Double maxBidPrice;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
    @Column(name = "owneruser_name", nullable = false)
    private String ownerUserName;
    @ManyToOne
    @JoinColumn(name = "winner_id", nullable = true) // ✅ winner สามารถเป็น null ได้
    private User winner;
    @OneToMany(mappedBy = "auction", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Bid> bids;
    private String imageUrl;


    @Enumerated(EnumType.STRING)
    private AuctionStatus status; // สถานะการประมูล

    public ZonedDateTime getStartTime() {
        return startTime.withZoneSameInstant(ZoneId.of("Asia/Bangkok"));
    }

    public void setStartTime(ZonedDateTime startTime) {
        this.startTime = startTime.withZoneSameInstant(ZoneId.of("Asia/Bangkok"));
    }

    public ZonedDateTime getEndTime() {
        return endTime.withZoneSameInstant(ZoneId.of("Asia/Bangkok"));
    }

    public void setEndTime(ZonedDateTime endTime) {
        this.endTime = endTime.withZoneSameInstant(ZoneId.of("Asia/Bangkok"));
    }


    // Getters และ Setters
    public int getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(int auctionId) {
        this.auctionId = auctionId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(double startingPrice) {
        this.startingPrice = startingPrice;
    }

    public Double getMaxBidPrice() {
        return maxBidPrice;
    }

    public void setMaxBidPrice(double maxBidPrice) {
        this.maxBidPrice = maxBidPrice;
    }



    public AuctionStatus getStatus() {
        return status;
    }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    public List<Bid> getBids() {
        return bids;
    }

    public void setBids(List<Bid> bids) {
        this.bids = bids;
    }
    public String getOwnerUserName() {
        return ownerUserName;
    }
    public void setOwnerUserName(String ownerUserName) {
        this.ownerUserName = ownerUserName;

    }
    public User getWinner() {
        return winner;
    }

    public void setWinner(User winner) {
        this.winner = winner;
    }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

}
