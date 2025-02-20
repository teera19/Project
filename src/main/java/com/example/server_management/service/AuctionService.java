package com.example.server_management.service;

import com.example.server_management.models.*;
import com.example.server_management.repository.AuctionRepository;
import com.example.server_management.repository.BidRepository;
import com.example.server_management.repository.MyAuctionRepository;
import com.example.server_management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AuctionService {

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private BidRepository bidRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MyAuctionRepository myAuctionRepository;

    public List<Auction> getWonAuctions(User user) {
        return auctionRepository.findByWinner(user);
    }

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
    public void closeAuction(int auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found"));

        if (auction.getStatus() != AuctionStatus.ONGOING) {
            throw new IllegalStateException("Auction is already closed.");
        }

        auction.setStatus(AuctionStatus.ENDED);

        // ✅ ค้นหาผู้ที่ประมูลสูงสุด และกำหนดเป็นผู้ชนะ
        Optional<Bid> highestBid = bidRepository.findTopByAuctionOrderByBidAmountDesc(auction);
        highestBid.ifPresent(bid -> auction.setWinner(bid.getUser()));

        auctionRepository.save(auction);
        finalizeAuction(auction); // ✅ บันทึกลง MyAuction
    }
    @Scheduled(fixedRate = 60000) // รันทุก 60 วินาที
    public void checkAndFinalizeAuctions() {
        List<Auction> expiredAuctions = auctionRepository.findByEndTimeBeforeAndStatus(
                LocalDateTime.now(), AuctionStatus.ONGOING
        );

        for (Auction auction : expiredAuctions) {
            auction.setStatus(AuctionStatus.ENDED);

            // หาผู้ที่ประมูลสูงสุด
            Optional<Bid> highestBid = bidRepository.findTopByAuctionOrderByBidAmountDesc(auction);
            highestBid.ifPresent(bid -> auction.setWinner(bid.getUser()));

            auctionRepository.save(auction);
            finalizeAuction(auction);
            auctionRepository.save(auction);
            finalizeAuction(auction); // ✅ บันทึกลง MyAuction
        }
    }
    public void finalizeAuction(Auction auction) {
        if (auction.getStatus() == AuctionStatus.ENDED && auction.getWinner() != null) {
            MyAuction myAuction = new MyAuction(auction, auction.getWinner());
            myAuctionRepository.save(myAuction);
        }
    }


}
