package com.ecommerce.mvc.service.product;

import com.ecommerce.mvc.entity.Product;

import java.util.List;

public interface ProductService {

    List<Product> getAllProducts();

    Product getProductById(String id);

    void deleteProduct(String id);

    void save(Product product);

}
