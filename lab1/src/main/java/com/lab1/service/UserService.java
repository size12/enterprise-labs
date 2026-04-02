package com.lab1.service;

import com.lab1.entity.User;

import java.util.Optional;

public interface UserService {

    User registerUser(String username, String password);

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
}
