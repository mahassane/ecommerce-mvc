package com.ecommerce.mvc.dao.user;

import com.ecommerce.mvc.entity.User;

import java.util.List;

public interface UserDAO {
    List<User> getAllUsers();
    User getUserById(String id);
    User getUserByEmail(String email);
    void updateUser(User user);
    void deleteUserById(String id);
    void save(User user);
}
