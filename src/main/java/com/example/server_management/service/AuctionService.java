package com.example.server_management.service;

import com.example.server_management.models.*;
import com.example.server_management.repository.AuctionRepository;
import com.example.server_management.repository.BidRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.ZoneId;
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

    @Transactional
    public Bid addBid(int auctionId, User user, double bidAmount) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found"));

        //  ใช้เวลา Bangkok
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Bangkok"));

        //  แปลงเวลาประมูลจาก UTC → Bangkok
        ZonedDateTime auctionStart = ZonedDateTime.of(auction.getStartTime(), ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Asia/Bangkok"));

        ZonedDateTime auctionEnd = ZonedDateTime.of(auction.getEndTime(), ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Asia/Bangkok"));

        //  ตรวจสอบว่าอยู่ในช่วงเวลาหรือไม่
        if (now.isBefore(auctionStart)) {
            throw new IllegalArgumentException("Auction has not started yet.");
        }

        if (now.isAfter(auctionEnd)) {
            throw new IllegalArgumentException("Auction has already ended.");
        }

        //  ตรวจสอบว่าบิดอยู่ในช่วงราคาหรือไม่
        if (bidAmount < auction.getStartingPrice() || bidAmount > auction.getMaxBidPrice()) {
            throw new IllegalArgumentException("Bid must be between " + auction.getStartingPrice() + " and " + auction.getMaxBidPrice() + ".");
        }

        //  ตรวจสอบว่ามีคนบิดราคาเดียวกันหรือไม่
        List<Bid> existingBids = bidRepository.findByAuctionAndBidAmount(auction, bidAmount);
        if (!existingBids.isEmpty()) {
            //  หา bid ที่บิดเร็วที่สุด
            Bid earliestBid = existingBids.stream()
                    .min((b1, b2) -> b1.getBidTime().compareTo(b2.getBidTime()))
                    .orElse(null);
            if (earliestBid != null) {
                auction.setWinner(earliestBid.getUser()); //  ให้คนที่บิดก่อนเป็นผู้ชนะ
            }
        }

        //  สร้าง Bid ใหม่ และบันทึกลงฐานข้อมูล
        Bid bid = new Bid();
        bid.setAuction(auction);
        bid.setUser(user);
        bid.setBidAmount(bidAmount);
        bid.setBidTime(LocalDateTime.now(ZoneId.of("UTC"))); //  ตั้งเวลาเป็น UTC

        return bidRepository.save(bid);
    }


    public List<Auction> getWonAuctions(User user) {
        return auctionRepository.findByWinner(user);
    }
}
