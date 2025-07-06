package com.ecommerce.mvc.dao.cartItem;

import com.ecommerce.mvc.entity.CartItem;
import com.ecommerce.mvc.entity.Product;
import com.ecommerce.mvc.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CartItemDAOImpl implements CartItemDAO {
    private EntityManager entityManager;

    @Autowired
    public CartItemDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<CartItem> getCartItems(User user) {
        return entityManager.createQuery("from CartItem where user.userId = :id", CartItem.class).setParameter("id", user.getUserId()).getResultList();
    }

    @Override
    @Transactional
    public void clearCart(User user) {
        entityManager.createQuery("delete from CartItem where user.userId = :id").setParameter("id", user.getUserId()).executeUpdate();
    }

    @Override
    public CartItem getCartItem(String id) {
        try {
            return entityManager.createQuery("from CartItem where cartItemId = :id", CartItem.class).setParameter("id", id).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public CartItem getUserCartItem(String userId, String Pid) {
        try {
            return entityManager.createQuery("from CartItem where user.userId = :userId and product.pid = :pid", CartItem.class).setParameter("userId", userId).setParameter("pid", Pid).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    @Transactional
    public void deleteCartItem(Product product) {
        entityManager.createQuery("DELETE from CartItem where product.pid =:id").setParameter("id", product.getPid()).executeUpdate();;
    }

    @Override
    @Transactional
    public void deleteUserCartItems(String userId) {
        User user =  entityManager.find(User.class, userId);
        entityManager.createQuery("delete from CartItem where user.userId = :id").setParameter("id", user.getUserId()).executeUpdate();
    }

    @Override
    @Transactional
    public void save(CartItem cartItem) {
        if (getCartItem(cartItem.getCartItemId()) == null) {
            entityManager.persist(cartItem);
        } else entityManager.merge(cartItem);
    }

}
