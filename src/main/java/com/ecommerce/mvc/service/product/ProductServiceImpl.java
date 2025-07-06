package com.ecommerce.mvc.service.product;

import com.ecommerce.mvc.dao.product.ProductDAO;
import com.ecommerce.mvc.entity.Product;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements  ProductService {

    private ProductDAO productDAO;

    @Autowired
    public ProductServiceImpl(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    @Override
    public List<Product> getAllProducts() {
        return productDAO.getAllProducts();
    }

    @Override
    public Product getProductById(String id) {
        return productDAO.getProductById(id);
    }

    @Override
    @Transactional
    public void deleteProduct(String id) {
        productDAO.deleteProduct(id);
    }

    @Override
    @Transactional
    public void save(Product product) {
        productDAO.save(product);
    }
}
