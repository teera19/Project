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

    List<Auction> findByOrderByAuctionIdDesc();
    List<Auction> findByWinner(User winner);
    List<Auction> findByStatus(AuctionStatus auctionStatus);

    // ✅ ดึงรายการสินค้าที่ผู้ใช้เป็นเจ้าของ
    @Query("SELECT a FROM Auction a WHERE a.ownerUserName = :ownerUserName ORDER BY a.startTime DESC")
    List<Auction> findByOwnerUserName(@Param("ownerUserName") String ownerUserName);
}
