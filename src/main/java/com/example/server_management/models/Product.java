package com.example.server_management.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;

import java.util.Base64;

@Entity
@Table(name = "product")
@JsonPropertyOrder({"productId", "name", "description", "price", "imageUrl", "categoryName", "shop"})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private int productId;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "price")
    private double price;

    @Lob
    @Column(name = "image", columnDefinition = "BLOB") // ✅ เก็บภาพแบบ Blob (ถ้าใช้)
    private byte[] image;

    @Column(name = "image_url")  // ✅ เพิ่ม image_url เข้าไปในฐานข้อมูล
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    @JsonBackReference
    private Category category;

    @ManyToOne
    @JoinColumn(name = "myshop_id", nullable = false)
    private MyShop shop;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnore
    private ClothingDetails clothingDetails;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnore
    private ShoesDetails shoesDetails;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnore
    private PhoneDetails phoneDetails;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnore
    private More more;

    // ✅ Getter / Setter
    public ClothingDetails getClothingDetails() {
        return clothingDetails;
    }

    // ✅ Getter สำหรับ ShoesDetails
    public ShoesDetails getShoesDetails() {
        return shoesDetails;
    }

    // ✅ Getter สำหรับ PhoneDetails
    public PhoneDetails getPhoneDetails() {
        return phoneDetails;
    }

    // ✅ Getter สำหรับ More
    public More getMore() {
        return more;
    }

    // ✅ Setter
    public void setClothingDetails(ClothingDetails clothingDetails) {
        this.clothingDetails = clothingDetails;
    }

    public void setShoesDetails(ShoesDetails shoesDetails) {
        this.shoesDetails = shoesDetails;
    }

    public void setPhoneDetails(PhoneDetails phoneDetails) {
        this.phoneDetails = phoneDetails;
    }

    public void setMore(More more) {
        this.more = more;
    }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public byte[] getImage() { return image; }
    public void setImage(byte[] image) { this.image = image; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public MyShop getShop() { return shop; }
    public void setShop(MyShop shop) { this.shop = shop; }
}
