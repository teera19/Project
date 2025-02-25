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
               COALESCE(b.highest_bidder, 'No bids'), COALESCE(b.highest_bid, 0) 
        FROM auction a
        LEFT JOIN (
            SELECT auction_id, 
                   MAX(bid_amount) AS highest_bid, 
                   (SELECT u.user_name FROM bid b2 
                    JOIN users u ON b2.user_id = u.user_id 
                    WHERE b2.auction_id = b1.auction_id 
                    ORDER BY b2.bid_amount DESC, b2.bid_time ASC LIMIT 1) AS highest_bidder
            FROM bid b1
            GROUP BY auction_id
        ) b ON a.auction_id = b.auction_id
        WHERE a.owneruser_name = :userName
    """, nativeQuery = true)
    List<Object[]> findAllAuctionsByOwner(@Param("userName") String userName);
    List<Auction> findByOrderByAuctionIdDesc();
    List<Auction> findByWinner(User winner);

    List<Auction> findByStatus(AuctionStatus auctionStatus);
}