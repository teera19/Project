package com.example.server_management.repository;

import com.example.server_management.models.Auction;
import com.example.server_management.models.AuctionStatus;
import com.example.server_management.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Integer> {
    @Query(value = """
    SELECT a.auction_id, a.product_name, a.description, 
           a.max_bid_price, a.start_time, a.end_time, a.image_url, a.status, 
           COALESCE(u.user_name, 'No bids') AS highest_bidder, 
           COALESCE(MAX(b.bid_amount), 0) AS highest_bid 
    FROM auction a
    LEFT JOIN bid b ON a.auction_id = b.auction_id
    LEFT JOIN users u ON b.user_id = u.user_id
    WHERE a.owneruser_name = :userName
    GROUP BY a.auction_id, a.product_name, a.description, 
             a.max_bid_price, a.start_time, a.end_time, 
             a.image_url, a.status, u.user_name
""", nativeQuery = true)
    List<Object[]> findAllAuctionsByOwner(@Param("userName") String userName);


    List<Auction> findByOrderByAuctionIdDesc();
    List<Auction> findByWinner(User winner);

    List<Auction> findByStatus(AuctionStatus auctionStatus);
}