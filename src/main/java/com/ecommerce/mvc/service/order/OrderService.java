package com.ecommerce.mvc.service.order;

import com.ecommerce.mvc.entity.Order;

import java.util.List;

public interface OrderService {

    List<Order> getOrders();

    List<Order> getUserOrders(String id);

    Order getOrderById(String id);

    void deleteOrderById(String id);

    void deleteUserOrders(String id);

    void save(Order order);
}
