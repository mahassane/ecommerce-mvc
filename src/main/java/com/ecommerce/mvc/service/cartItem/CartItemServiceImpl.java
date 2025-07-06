package com.ecommerce.mvc.service.cartItem;

import com.ecommerce.mvc.dao.cartItem.CartItemDAO;
import com.ecommerce.mvc.entity.CartItem;
import com.ecommerce.mvc.entity.Product;
import com.ecommerce.mvc.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartItemServiceImpl implements CartItemService {

    private CartItemDAO cartItemDAO;

    @Autowired
    public CartItemServiceImpl(CartItemDAO cartItemDAO) {
        this.cartItemDAO = cartItemDAO;
    }

    @Override
    public List<CartItem> getCartItems(User user) {
        return cartItemDAO.getCartItems(user);
    }

    @Override
    @Transactional
    public void clearCart(User user) {
        cartItemDAO.clearCart(user);
    }

    @Override
    public CartItem getCartItem(String id) {
        return cartItemDAO.getCartItem(id);
    }

    @Override
    public CartItem getUserCartItem(String userId, String Pid) {
        return cartItemDAO.getUserCartItem(userId, Pid);
    }

    @Override
    @Transactional
    public void deleteCartItem(Product product) {
        cartItemDAO.deleteCartItem(product);
    }

    @Override
    @Transactional
    public void deleteUserCartItems(String userId) {
        cartItemDAO.deleteUserCartItems(userId);
    }

    @Override
    @Transactional
    public void save(CartItem cartItem) {
        cartItemDAO.save(cartItem);
    }
}
