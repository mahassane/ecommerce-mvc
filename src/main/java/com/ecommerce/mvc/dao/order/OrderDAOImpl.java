package com.ecommerce.mvc.dao.order;


import com.ecommerce.mvc.entity.Order;
import com.ecommerce.mvc.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderDAOImpl implements OrderDAO {

    private EntityManager  entityManager;

    @Autowired
    public OrderDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<Order> getOrders() {
        return  entityManager.createQuery("from Order", Order.class).getResultList();
    }

    @Override
    public List<Order> getUserOrders(String id) {
        return entityManager.createQuery("from Order where userId.userId = :id").setParameter("id", id).getResultList();
    }

    @Override
    public Order getOrderById(String id) {
        return entityManager.find(Order.class, id);
    }

    @Override
    @Transactional
    public void deleteOrderById(String id) {
        Order order = entityManager.find(Order.class, id);
        if (order != null) {
            entityManager.remove(order);
        }
    }

    @Override
    @Transactional
    public void deleteUserOrders(String id) {
        User user = entityManager.find(User.class, id);
        if (user != null) {
            entityManager.createQuery("delete from Order where userId.userId = :id").setParameter("id", id).executeUpdate();
        }
    }


    @Override
    @Transactional
    public void save(Order order) {
        entityManager.persist(order);
    }
}
