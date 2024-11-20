package com.example.server_management.repository;

import com.example.server_management.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO product (name, description, price, image, shop_id) VALUES (:name, :description, :price, :image, :shopId)", nativeQuery = true)
    int createProduct(@Param("name") String name,
                      @Param("description") String description,
                      @Param("price") double price,
                      @Param("image") byte[] image,
                      @Param("shopId") int shopId);
}
