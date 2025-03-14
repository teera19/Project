package com.example.server_management.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.List;


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

    @Column(name = "qr_code_url")
    private String qrCodeUrl;

    @Column(name = "bank_account_number")
    private String bankAccountNumber; // เลขบัญชีธนาคาร

    @Column(name = "display_name") // เปลี่ยนจาก bank_account_name เป็น display_name
    private String displayName; // ชื่อบัญชีธนาคาร (หรือชื่อผู้รับ)

    @Column(name = "bank_name")
    private String bankName; // ชื่อธนาคาร

    @OneToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    // Getter และ Setter
    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    // ส่วนอื่นๆ
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

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }
}