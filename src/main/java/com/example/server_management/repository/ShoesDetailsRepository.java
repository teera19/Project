package com.example.server_management.repository;

import com.example.server_management.models.Product;
import com.example.server_management.models.ShoesDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShoesDetailsRepository extends JpaRepository<ShoesDetails, Integer> {
    Optional<ShoesDetails>  findByProduct(Product product);
}