package com.ecommerce.mvc.dao.product;

import com.ecommerce.mvc.entity.Product;

import java.util.List;

public interface ProductDAO {
    List<Product> getAllProducts();
    Product getProductById(String id);
    void deleteProduct(String id);
    void updateProduct(Product product);
    void save(Product product);
}
