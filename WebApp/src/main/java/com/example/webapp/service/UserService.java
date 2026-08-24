package com.example.webapp.service;

import com.example.webapp.model.User;
import com.example.webapp.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User createUser(String username, String rawPassword, boolean manageUsers, boolean manageStock, boolean viewAllOrders) {
        User user = new User(username, passwordEncoder.encode(rawPassword), manageUsers, manageStock, viewAllOrders);
        return userRepository.save(user);
    }

    public User updatePermissions(Long id, boolean manageUsers, boolean manageStock, boolean viewAllOrders) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Unknown user: " + id));
        user.setManageUsers(manageUsers);
        user.setManageStock(manageStock);
        user.setViewAllOrders(viewAllOrders);
        return userRepository.save(user);
    }
}
