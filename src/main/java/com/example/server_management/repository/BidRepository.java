package com.example.server_management.repository;

import com.example.server_management.models.Auction;
import com.example.server_management.models.Bid;
import com.example.server_management.models.BidHistory;
import com.example.server_management.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BidRepository extends JpaRepository<Bid, Integer> {
    List<Bid> findByAuctionOrderByBidAmountDesc(Auction auction);
    List<Bid> findByAuction(Auction auction);
    List<Bid> findByAuction_AuctionId(int auctionId);;
    @Query("SELECT DISTINCT a FROM Auction a INNER JOIN Bid b ON b.auction = a WHERE b.user = :user")
    List<Auction> findDistinctAuctionsByUser(@Param("user") User user);


    List<Bid> findByAuctionAndBidAmount(Auction auction, double bidAmount);
}