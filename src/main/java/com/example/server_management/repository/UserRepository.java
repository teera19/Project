package com.example.server_management.repository;

import com.example.server_management.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;



@Repository
public interface UserRepository extends CrudRepository<User, Integer> {
    int countByUserName(String userName);

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO USERS(user_name, name, last_name, email, password, address, tel) VALUES(:user_name, :name, :last_name, :email, :password, :address, :tel)", nativeQuery = true)
    int register(@Param("user_name") String user_name,
                 @Param("name") String name,
                 @Param("last_name") String last_name,
                 @Param("email") String email,
                 @Param("password") String password,
                 @Param("address") String address,
                 @Param("tel") String tel);



    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.name = :name, u.lastName = :last_name, u.address = :address, u.tel = :tel WHERE u.userName = :user_name")
    int updateUserInfo(@Param("name") String name,
                       @Param("last_name") String last_name,
                       @Param("address") String address,
                       @Param("tel") String tel,
                       @Param("user_name") String user_name);
    User findByUserName(String user_name);

}

