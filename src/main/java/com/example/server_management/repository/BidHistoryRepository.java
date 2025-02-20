package com.example.server_management.repository;

import com.example.server_management.models.Auction;
import com.example.server_management.models.BidHistory;
import com.example.server_management.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface BidHistoryRepository extends JpaRepository<BidHistory, Long> {

    // ✅ ดึงเฉพาะ auctionId เพื่อลดโหลดข้อมูลซ้ำซ้อน
    @Query("SELECT bh.auction.auctionId FROM BidHistory bh WHERE bh.user = :user AND bh.isWinner = true")
    List<Integer> findWinningAuctionIdsByUser(@Param("user") User user);

    // ✅ ใช้เมธอดนี้ถ้าต้องการโหลด Auction พร้อมกัน (ไม่แนะนำถ้าข้อมูลเยอะ)
    List<BidHistory> findByUserAndIsWinnerTrue(User user);
    @Modifying
    @Transactional
    @Query("DELETE FROM BidHistory b WHERE b.auction = :auction AND b.isWinner = true")
    void deleteByAuction(@Param("auction") Auction auction);
}
