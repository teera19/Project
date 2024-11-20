package com.example.server_management.repository;

import com.example.server_management.models.MyShop;
import com.example.server_management.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface MyshopRepository extends JpaRepository<MyShop, Integer> {

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO MY_SHOP(title, detail, user_id) VALUES(:title, :detail, :userId)", nativeQuery = true)
    int createshop(@Param("title") String title,
                   @Param("detail") String detail,
                   @Param("userId") int userId);

    @Transactional
    @Modifying
    @Query("UPDATE MyShop m SET m.title = :title, m.detail = :detail WHERE m.user = :user")
    int updateShopInfo(@Param("title") String title,
                       @Param("detail") String detail,
                       @Param("user") User user);

    Optional<MyShop> findByUser(User user);
    MyShop findByTitle(String title);



}
