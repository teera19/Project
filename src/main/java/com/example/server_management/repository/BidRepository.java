package com.example.server_management.repository;

import com.example.server_management.models.Auction;
import com.example.server_management.models.Bid;
import com.example.server_management.models.BidHistory;
import com.example.server_management.models.User;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.EntityGraph;
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
    @Query(value = """
        SELECT DISTINCT 
            a.auction_id, 
            a.product_name, 
            a.description, 
            a.starting_price, 
            a.max_bid_price, 
            a.start_time, 
            a.end_time, 
            a.image_url, 
            a.status 
        FROM auction a 
        INNER JOIN bid b ON b.auction_id = a.auction_id 
        WHERE b.user_id = :userId
    """, nativeQuery = true)
    List<Tuple> findAllParticipatedAuctionsNative(@Param("userId") int userId);



    List<Bid> findByAuctionAndBidAmount(Auction auction, double bidAmount);
}