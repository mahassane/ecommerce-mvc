package com.ecommerce.mvc.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity
@Table(name="orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name="order_id")
    private String orderId;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User userId;

    @Column(name="quantity")
    private int quantity;

    @Column(name="grand_total")
    private float grand_total;

    @Column(name="created_at", updatable = false, insertable = false)
    private LocalDateTime created_at;

    @Column(name="full_name")
    private String full_name;

    @Column(name="email")
    private String email;

    @Column(name="address")
    private String address;

    @Column(name="payment_method")
    private String payment_method;

    public Order() {}

    public Order(User userId, int quantity, float grand_total, String full_name, String email, String address, String payment_method) {
        this.userId = userId;
        this.quantity = quantity;
        this.grand_total = grand_total;
        this.full_name = full_name;
        this.email = email;
        this.address = address;
        this.payment_method = payment_method;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public float getGrandTotal() {
        return grand_total;
    }

    public void setGrandTotal(float grand_total) {
        this.grand_total = grand_total;
    }

    public String getOrderId() {
        return orderId;
    }

    public User getUserId() {
        return userId;
    }

    public LocalDateTime getCreatedAt() {
        return created_at;
    }

    public String getFullName() {
        return full_name;
    }

    public void setFullName(String full_name) {
        this.full_name = full_name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPaymentMethod() {
        return payment_method;
    }

    public void setPaymentMethod(String payment_method) {
        this.payment_method = payment_method;
    }
}
