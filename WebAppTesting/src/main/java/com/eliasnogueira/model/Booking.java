package com.ryefry.model;

import com.ryefry.enums.RoomType;

import java.util.Objects;

public record Booking(String email, String country, String password, String dailyBudget, boolean newsletter,
                      RoomType roomType, String roomDescription) {

    public Booking {
        Objects.requireNonNull(email, "email must not be null");
        Objects.requireNonNull(country, "country must not be null");
        Objects.requireNonNull(password, "password must not be null");
        Objects.requireNonNull(dailyBudget, "dailyBudget must not be null");
        Objects.requireNonNull(roomType, "roomType must not be null");
        Objects.requireNonNull(roomDescription, "roomDescription must not be null");

        if (email.isBlank()) {
            throw new IllegalArgumentException("email must not be blank");
        }
    }
}
