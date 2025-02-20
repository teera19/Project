package com.example.server_management.service;

import com.example.server_management.models.*;
import com.example.server_management.repository.AuctionRepository;
import com.example.server_management.repository.BidRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found"));

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Bangkok"));
        ZonedDateTime auctionStart = auction.getStartTime().atZone(ZoneId.of("Asia/Bangkok"));
        ZonedDateTime auctionEnd = auction.getEndTime().atZone(ZoneId.of("Asia/Bangkok"));

        if (now.isBefore(auctionStart)) {
            throw new IllegalArgumentException("Auction has not started yet.");
        }

        if (now.isAfter(auctionEnd)) {
            throw new IllegalArgumentException("Auction has already ended.");
        }

        Bid bid = new Bid();
        bid.setAuction(auction);
        bid.setUser(user);
        bid.setBidAmount(bidAmount);
        return bidRepository.save(bid);
    }
    public List<Auction> getWonAuctions(User user) {
        return auctionRepository.findByWinner(user);
    }
}