package com.ryefry.enums;

import java.util.function.Supplier;

public enum RoomType implements Supplier<String> {

    SINGLE("Single"), FAMILY("Family"), BUSINESS("Business");

    private final String value;

    RoomType(String value) {
        this.value = value;
    }

    @Override
    public String get() {
        return this.value;
    }
}
