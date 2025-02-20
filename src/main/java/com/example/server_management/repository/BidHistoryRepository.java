package com.example.server_management.repository;

import com.example.server_management.models.Auction;
import com.example.server_management.models.BidHistory;
import com.example.server_management.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface BidHistoryRepository extends JpaRepository<BidHistory, Long> {

    Page<BidHistory> findByUserAndIsWinnerTrue(User user, Pageable pageable);

    @Modifying
    @Transactional
    @Query("DELETE FROM BidHistory b WHERE b.auction = :auction AND b.isWinner = true")
    void deleteByAuction(@Param("auction") Auction auction);
}
