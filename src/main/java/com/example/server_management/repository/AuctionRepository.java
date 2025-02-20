package com.example.server_management.repository;

import com.example.server_management.models.Auction;
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
    @Query("SELECT a FROM Auction a WHERE a.auctionId IN :auctionIds")
    List<Auction> findAllByAuctionIds(@Param("auctionIds") List<Integer> auctionIds);
}

