package com.ecommerce.mvc.service.order;

import com.ecommerce.mvc.dao.order.OrderDAO;
import com.ecommerce.mvc.entity.Order;
import com.ecommerce.mvc.service.product.ProductService;
import com.ecommerce.mvc.service.user.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private OrderDAO orderDAO;

    @Autowired
    public OrderServiceImpl(OrderDAO orderDAO) {
        this.orderDAO = orderDAO;
    }

    @Override
    public List<Order> getOrders() {
        return orderDAO.getOrders();
    }

    @Override
    public List<Order> getUserOrders(String id) {
        return orderDAO.getUserOrders(id);
    }

    @Override
    public Order getOrderById(String id) {
        return orderDAO.getOrderById(id);
    }

    @Override
    @Transactional
    public void deleteOrderById(String id) {
        orderDAO.deleteOrderById(id);
    }

    @Override
    @Transactional
    public void deleteUserOrders(String id) {
        orderDAO.deleteUserOrders(id);
    }

    @Override
    @Transactional
    public void save(Order order) {
        orderDAO.save(order);
    }
}
