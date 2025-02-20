package com.example.server_management.dto;

import com.example.server_management.models.Auction;

public class AuctionSummaryResponse {
    private int auctionId;
    private String productName;
    private String description;
    private String imageUrl;

    public AuctionSummaryResponse(Auction auction) {
        this.auctionId = auction.getAuctionId();
        this.productName = auction.getProductName();
        this.description = auction.getDescription();
        this.imageUrl = "/images/" + auction.getAuctionId() + ".jpg";

    }

    // ✅ Getters
    public int getAuctionId() { return auctionId; }
    public String getProductName() { return productName; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
}

