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
    Bid findTopByAuctionOrderByBidAmountDesc(Auction auction);
    List<Bid> findByAuction(Auction auction);
    List<Bid> findByAuction_AuctionId(int auctionId);;
    List<Bid> findByAuctionAndUser(Auction auction, User user);
    @Query(value = """
    SELECT DISTINCT a.auction_id, a.product_name, a.description, 
           COALESCE(b2.bid_amount, 0) as highest_bid, 
           COALESCE(u.user_name, 'No Bids') as highest_bidder,
           a.max_bid_price, 
           a.start_time, a.end_time, a.image_url, a.status 
    FROM auction a
    LEFT JOIN bid b2 ON b2.auction_id = a.auction_id 
                      AND b2.bid_amount = (SELECT MAX(b1.bid_amount) FROM bid b1 WHERE b1.auction_id = a.auction_id)
    LEFT JOIN users u ON u.user_id = b2.user_id
    INNER JOIN bid b ON b.auction_id = a.auction_id
    WHERE b.user_id = :userId
""", nativeQuery = true)
    List<Object[]> findAllParticipatedAuctions(@Param("userId") int userId);

    List<Bid> findByAuctionAndBidAmount(Auction auction, double bidAmount);
}