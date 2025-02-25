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
import java.util.Optional;

public interface BidHistoryRepository extends JpaRepository<BidHistory, Long> {

    //  ดึงเฉพาะรายการที่ผู้ใช้ชนะ

    List<BidHistory> findByUserAndIsWinnerTrue(User user);
    @Modifying
    @Transactional
    @Query("DELETE FROM BidHistory b WHERE b.auction = :auction AND b.isWinner = true")
    void deleteByAuction(@Param("auction") Auction auction);


}