package com.ecommerce.mvc.entity;

import jakarta.persistence.*;

@Entity
@Table(name="order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String item_id;

    @ManyToOne
    @JoinColumn(name="order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name="pid")
    private Product product;

    @Column(name="quantity")
    private int quantity;

    public OrderItem() {}

    public OrderItem(Order order, Product product, int quantity) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
    }

    public Order getOrder() {
        return order;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getItem_id() {
        return item_id;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
