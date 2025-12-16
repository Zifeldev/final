package com.example.banking.config;

public enum RepositoryType {
    POSTGRES,
    MONGO,
    IN_MEMORY;

    public static RepositoryType from(String value) {
        if (value == null || value.isBlank()) {
            return IN_MEMORY;
        }
        return RepositoryType.valueOf(value.trim().toUpperCase());
    }
}
