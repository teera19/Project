package com.example.server_management.models;

import jakarta.persistence.*;

@Entity
@Table(name = "my_shop")
public class MyShop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "myshop_id")
    private int myShopId;

    @Column(name = "title")
    private String title;

    @Column(name = "detail")
    private String detail;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public MyShop() {
    }

    public int getMyShopId() {
        return myShopId;
    }

    public void setMyShopId(int myShopId) {
        this.myShopId = myShopId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
