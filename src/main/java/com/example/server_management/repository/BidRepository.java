package com.example.server_management.repository;

import com.example.server_management.models.Auction;
import com.example.server_management.models.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepository extends JpaRepository<Bid, Integer> {
    List<Bid> findByAuctionOrderByBidAmountDesc(Auction auction);
    List<Bid> findByAuction(Auction auction);
    List<Bid> findByAuction_AuctionId(int auctionId);
    Optional<Bid> findTopByAuctionOrderByBidAmountDesc(Auction auction);
}
