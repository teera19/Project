package com.example.server_management.repository;

import com.example.server_management.models.ClothingDetails;
import com.example.server_management.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClothingDetailsRepository extends JpaRepository<ClothingDetails, Integer> {
    Optional<ClothingDetails>  findByProduct(Product product);
}

