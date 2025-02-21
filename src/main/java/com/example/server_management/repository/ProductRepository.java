package com.example.server_management.repository;

import com.example.server_management.models.Category;
import com.example.server_management.models.MyShop;
import com.example.server_management.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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

    List<Product> findByShop(MyShop shop);
    List<Product> findByCategory(Category category);
    List<Product> findByNameContainingIgnoreCase(String name);
    Optional<Product> findById(int productId);
    @Query("SELECT m.user.userName FROM MyShop m WHERE m.id = (SELECT p.shop.id FROM Product p WHERE p.id = :productId)")
    String findSellerByProductId(@Param("productId") int productId);


}