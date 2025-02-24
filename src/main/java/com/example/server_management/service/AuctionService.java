package com.example.server_management.service;

import com.example.server_management.models.*;
import com.example.server_management.repository.AuctionRepository;
import com.example.server_management.repository.BidRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        //  แปลงเวลาจาก UTC เป็น Bangkok ก่อนเปรียบเทียบ
        ZonedDateTime auctionStart = ZonedDateTime.of(auction.getStartTime(), ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Asia/Bangkok"));

        ZonedDateTime auctionEnd = ZonedDateTime.of(auction.getEndTime(), ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of("Asia/Bangkok"));

        //  ห้ามบิดก่อนที่ประมูลจะเริ่ม
        if (now.isBefore(auctionStart)) {
            throw new IllegalArgumentException("Auction has not started yet.");
        }

        //  ตรวจสอบว่าหมดเวลาแล้วหรือยัง
        if (now.isAfter(auctionEnd)) {
            throw new IllegalArgumentException("Auction has already ended.");
        }

        //  ตรวจสอบว่าการบิดอยู่ในช่วงที่กำหนด
        if (bidAmount < auction.getStartingPrice() || bidAmount > auction.getMaxBidPrice()) {
            throw new IllegalArgumentException("Bid must be between " + auction.getStartingPrice() + " and " + auction.getMaxBidPrice() + ".");
        }

        // ถ้ามีคนบิด 5000 บาท ให้เป็นผู้ชนะทันที
        if (bidAmount == auction.getMaxBidPrice()) {
            auction.setWinner(user);
            auction.setStatus(AuctionStatus.COMPLETED); //  ปิดประมูลทันที
        }

        if (bidAmount > auction.getMaxBidPrice()) {
            auction.setMaxBidPrice(bidAmount);
            auctionRepository.save(auction);
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
