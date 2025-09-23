package com.example.springproject.service;

import com.example.springproject.model.User;
import com.example.springproject.repository.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private MealService mealService;

    @Override
    public void save(User user) {
        try {
            if (!user.getPassword().startsWith("$2a$")) {
                user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
            }
            userDao.save(user);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public User findById(String id) {
        try {
            return userDao.findById(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public User findByRole(String role) {
        try {
            return userDao.findByRole(role);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(User user) {
        try {
            mealService.deleteByUser(user);
            userDao.deleteById(user.getId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(User user) {
        try {
            userDao.update(user);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
