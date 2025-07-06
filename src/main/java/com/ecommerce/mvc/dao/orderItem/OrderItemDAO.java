package com.ecommerce.mvc.dao.orderItem;

import com.ecommerce.mvc.entity.OrderItem;

import java.util.List;

public interface OrderItemDAO {
    List<OrderItem> getOrderItems(String orderId);
    void deleteOrderItemsByProduct(String productId);
    void deleteOrderItemById(String id);
    void deleteOrderItems(String id);
    void save(OrderItem orderItem);
}
