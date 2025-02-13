package com.example.server_management.service;

import com.example.server_management.models.*;
import com.example.server_management.repository.AuctionRepository;
import com.example.server_management.repository.BidHistoryRepository;
import com.example.server_management.repository.BidRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class AuctionService {

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private BidRepository bidRepository;
    @Autowired
    private BidHistoryRepository bidHistoryRepository;  // ✅ ใช้ Repository ที่ extends JpaRepository


    public List<Auction> getAllAuctions() {
        return auctionRepository.findByOrderByAuctionIdDesc();
    }

    public Auction getAuctionById(int auctionId) {
        return auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found with ID: " + auctionId));
    }

    public Auction addAuction(Auction auction) {
        System.out.println("   Saving Auction...");
        System.out.println("   Product Name: " + auction.getProductName());
        System.out.println("   Starting Price: " + auction.getStartingPrice());
        System.out.println("   Max Bid Price: " + auction.getMaxBidPrice());
        System.out.println("   Owner User Name: " + auction.getOwnerUserName());
        System.out.println("   Start Time: " + auction.getStartTime());
        System.out.println("   End Time: " + auction.getEndTime());

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

    @Transactional
    public void determineAuctionWinner(Auction auction) {
        List<Bid> bids = bidRepository.findByAuction(auction);
        if (bids.isEmpty()) {
            auction.setStatus(AuctionStatus.ENDED);
            auctionRepository.save(auction);
            return;
        }

        Bid highestBid = bids.stream().max(Comparator.comparingDouble(Bid::getBidAmount)).orElse(null);
        if (highestBid != null) {
            closeAuctionWithWinner(auction, highestBid);
            auctionRepository.flush();  // ✅ บังคับให้บันทึก
        }
    }



    @Transactional
    private void closeAuctionWithWinner(Auction auction, Bid highestBid) {
        auction.setWinner(highestBid.getUser());
        auction.setStatus(AuctionStatus.COMPLETED);

        System.out.println("🏆 Closing auction: " + auction.getAuctionId());
        System.out.println("🎯 Winner: " + highestBid.getUser().getUserName());

        auctionRepository.save(auction);  // ✅ บันทึกข้อมูลผู้ชนะก่อน

        List<Bid> bids = bidRepository.findByAuction(auction);
        for (Bid bid : bids) {
            boolean isWinner = bid.getUser().equals(highestBid.getUser());

            System.out.println("📜 Saving BidHistory -> User: " + bid.getUser().getUserName() +
                    " | Amount: " + bid.getBidAmount() +
                    " | Winner: " + isWinner);

            BidHistory bidHistory = new BidHistory(
                    bid.getUser(), auction, bid.getBidAmount(),
                    bid.getBidTime() != null ? bid.getBidTime() : LocalDateTime.now(),  // ✅ ป้องกัน bidTime เป็น null
                    isWinner
            );

            bidHistoryRepository.save(bidHistory);
        }
    }






    @Transactional
    public void updateAuctionStatus(Auction auction) {
        try {
            auctionRepository.save(auction); // ✅ บันทึกสถานะใหม่ลงฐานข้อมูล
            System.out.println(" Auction " + auction.getAuctionId() + " updated to " + auction.getStatus());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(" Error updating auction status: " + e.getMessage());
        }
    }

    @Scheduled(fixedRate = 600000) // ✅ อัปเดตทุกๆ 10 นาที (600,000 ms)
    public void checkAuctionsForWinners() {
        List<Auction> auctions = auctionRepository.findAll();
        for (Auction auction : auctions) {
            determineAuctionWinner(auction);
        }
    }
    public List<Auction> getAuctionsByWinner(User winner) {
        return auctionRepository.findByWinner(winner);
    }
}