package com.example.springproject.service;

import com.example.springproject.model.User;

public interface UserService {
    void save(User user);

    User findById(String id);

    User findByRole(String role);

    void delete(User user);

    void update(User user);

}
