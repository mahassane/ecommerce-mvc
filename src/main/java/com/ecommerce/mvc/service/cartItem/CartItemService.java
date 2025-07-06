package com.ecommerce.mvc.service.cartItem;

import com.ecommerce.mvc.entity.CartItem;
import com.ecommerce.mvc.entity.Product;
import com.ecommerce.mvc.entity.User;

import java.util.List;

public interface CartItemService {

    List<CartItem> getCartItems(User user);

    void clearCart(User user);

    CartItem getCartItem(String id);

    CartItem getUserCartItem(String userId, String Pid);

    void deleteCartItem(Product product);

    void deleteUserCartItems(String userId);

    void save(CartItem cartItem);
}
