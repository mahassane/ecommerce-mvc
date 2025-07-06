package com.ecommerce.mvc.dao.product;

import com.ecommerce.mvc.entity.Product;
import com.ecommerce.mvc.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductDAOImpl implements ProductDAO {

    private EntityManager entityManager;

    @Autowired
    public ProductDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<Product> getAllProducts() {
        return entityManager.createQuery("from Product", Product.class).getResultList();
    }

    @Override
    public Product getProductById(String id) {
        return entityManager.find(Product.class, id);
    }

    @Override
    @Transactional
    public void deleteProduct(String id) {
        Product product = entityManager.find(Product.class, id);
        if (product != null) {
            entityManager.remove(product);
        }
    }

    @Override
    @Transactional
    public void updateProduct(Product product) {
        entityManager.merge(product);
    }

    @Override
    @Transactional
    public void save(Product product) {
        entityManager.persist(product);
    }
}
