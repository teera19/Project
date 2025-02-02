package com.example.server_management.service;

import com.example.server_management.models.Auction;
import com.example.server_management.models.AuctionStatus;
import com.example.server_management.models.Bid;
import com.example.server_management.models.User;
import com.example.server_management.repository.AuctionRepository;
import com.example.server_management.repository.BidRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuctionService {

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private BidRepository bidRepository;

    public List<Auction> getAllAuctions() {
        return auctionRepository.findByOrderByAuctionIdDesc();
    }

    public Auction getAuctionById(int auctionId) {
        return auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found with ID: " + auctionId));
    }

    public Auction addAuction(Auction auction) {
        return auctionRepository.save(auction);
    }

    public Bid addBid(int auctionId, User user, double bidAmount) {
        // ดึงข้อมูลการประมูล
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found"));

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(auction.getStartTime())) {
            throw new IllegalArgumentException("Auction has not started yet.");
        }
        if (now.isAfter(auction.getEndTime())) {
            throw new IllegalArgumentException("Auction has already ended.");
        }

        // ตรวจสอบราคาบิด
        if (bidAmount < auction.getStartingPrice()) {
            throw new IllegalArgumentException("Bid amount must be at least the starting price.");
        }

        // สร้างรายการบิดใหม่
        Bid bid = new Bid();
        bid.setAuction(auction);
        bid.setUser(user);
        bid.setBidAmount(bidAmount);
        bidRepository.save(bid);

        // ตรวจสอบราคาสูงสุด (maxBidPrice)
        if (bidAmount >= auction.getMaxBidPrice()) {
            // ปิดการประมูล
            auction.setStatus(AuctionStatus.COMPLETED);
            auction.setEndTime(LocalDateTime.now());
            auctionRepository.save(auction);
        }

        return bid;
    }
}