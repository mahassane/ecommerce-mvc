package com.ecommerce.mvc.service.orderItem;

import com.ecommerce.mvc.dao.orderItem.OrderItemDAO;
import com.ecommerce.mvc.entity.OrderItem;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderItemServiceImpl implements OrderItemService {

    private OrderItemDAO orderItemDAO;

    @Autowired
    public OrderItemServiceImpl(OrderItemDAO orderItemDAO) {
        this.orderItemDAO = orderItemDAO;
    }

    @Override
    public List<OrderItem> getOrderItems(String orderId) {
        return orderItemDAO.getOrderItems(orderId);
    }

    @Override
    @Transactional
    public void deleteOrderItemsByProduct(String productId) {
        orderItemDAO.deleteOrderItemsByProduct(productId);
    }

    @Override
    @Transactional
    public void deleteOrderItemById(String id) {
        orderItemDAO.deleteOrderItemById(id);
    }

    @Override
    @Transactional
    public void deleteOrderItems(String id) {
        orderItemDAO.deleteOrderItems(id);
    }

    @Override
    @Transactional
    public void save(OrderItem orderItem) {
        orderItemDAO.save(orderItem);
    }
}
