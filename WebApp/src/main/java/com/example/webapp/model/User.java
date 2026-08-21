package com.example.webapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private boolean manageUsers;

    @Column(nullable = false)
    private boolean manageStock;

    @Column(nullable = false)
    private boolean viewAllOrders;

    protected User() {
    }

    public User(String username, String passwordHash, boolean manageUsers, boolean manageStock, boolean viewAllOrders) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.manageUsers = manageUsers;
        this.manageStock = manageStock;
        this.viewAllOrders = viewAllOrders;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public boolean isManageUsers() { return manageUsers; }
    public void setManageUsers(boolean manageUsers) { this.manageUsers = manageUsers; }
    public boolean isManageStock() { return manageStock; }
    public void setManageStock(boolean manageStock) { this.manageStock = manageStock; }
    public boolean isViewAllOrders() { return viewAllOrders; }
    public void setViewAllOrders(boolean viewAllOrders) { this.viewAllOrders = viewAllOrders; }
}
