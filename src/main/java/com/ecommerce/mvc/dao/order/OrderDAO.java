package com.ecommerce.mvc.dao.order;

import com.ecommerce.mvc.entity.CartItem;
import com.ecommerce.mvc.entity.Order;
import com.ecommerce.mvc.entity.User;

import java.util.List;

public interface OrderDAO {
    List<Order> getOrders();
    List<Order> getUserOrders(String id);
    Order getOrderById(String id);
    void deleteOrderById(String id);
    void deleteUserOrders(String id);
    void save(Order order);
}
