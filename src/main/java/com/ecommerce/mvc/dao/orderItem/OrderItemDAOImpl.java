package com.ecommerce.mvc.dao.orderItem;

import com.ecommerce.mvc.entity.Order;
import com.ecommerce.mvc.entity.OrderItem;
import com.ecommerce.mvc.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderItemDAOImpl implements OrderItemDAO {

    private EntityManager entityManager;

    @Autowired
    public OrderItemDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<OrderItem> getOrderItems(String orderId) {
        return entityManager.createQuery("from OrderItem where order.orderId = :id").setParameter("id", orderId).getResultList();
    }

    @Override
    @Transactional
    public void deleteOrderItemsByProduct(String productId) {
        List<OrderItem> orderItems = entityManager.createQuery("from OrderItem where product.pid = :id").setParameter("id", productId).getResultList();
        if (orderItems != null) {
            for (OrderItem orderItem : orderItems) {
                entityManager.remove(orderItem);
            }
        }
    }

    @Override
    @Transactional
    public void deleteOrderItemById(String id) {
        OrderItem orderItem = entityManager.find(OrderItem.class, id);
        if (orderItem != null) {
            entityManager.remove(orderItem);
        }
    }

    @Override
    @Transactional
    public void deleteOrderItems(String id) {
        entityManager.createQuery("delete from OrderItem where order.orderId = :id").setParameter("id", id).executeUpdate();
    }

    @Override
    @Transactional
    public void save(OrderItem orderItem) {
        entityManager.persist(orderItem);
    }
}
