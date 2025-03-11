package com.example.server_management.models;

import jakarta.persistence.*;


@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int orderItemId;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order; // เชื่อมโยงกับ Order

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; // เชื่อมโยงกับ Product

    @Column(nullable = false)
    private int quantity; // จำนวนสินค้าที่สั่ง

    // Getter & Setter
    public int getOrderItemId() { return orderItemId; }

    public Order getOrder() { return order; }

    public void setOrder(Order order) { this.order = order; }

    public Product getProduct() { return product; }

    public void setProduct(Product product) { this.product = product; }

    public int getQuantity() { return quantity; }

    public void setQuantity(int quantity) { this.quantity = quantity; }
}
