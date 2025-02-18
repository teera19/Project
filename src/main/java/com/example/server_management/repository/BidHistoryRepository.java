package com.example.server_management.repository;

import com.example.server_management.models.Auction;
import com.example.server_management.models.Bid;
import com.example.server_management.models.BidHistory;
import com.example.server_management.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BidHistoryRepository extends JpaRepository<BidHistory, Long> {

    // ✅ ดึงเฉพาะรายการที่ผู้ใช้ชนะ

    List<BidHistory> findByUserAndIsWinnerTrue(User user);
    List<BidHistory> findByAuction_AuctionId(int auctionId);

}
