package com.ecommerce.mvc.entity;

import jakarta.persistence.*;

@Entity
@Table(name="products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name="pid")
    private String pid;

    @Column(name="pname")
    private String pname;

    @Column(name="price")
    private float price;

    @Column(name="description")
    private String description;

    @Column(name="image_url")
    private String imageUrl;

    public Product() {}

    public Product(String pname, float price, String description, String imageUrl) {
        this.pname = pname;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public String getPname() {
        return pname;
    }

    public void setPname(String pname) {
        this.pname = pname;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPid() {
        return pid;
    }
}
