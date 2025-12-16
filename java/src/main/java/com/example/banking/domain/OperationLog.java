package com.example.banking.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

public record OperationLog(
    String id,
        String accountId,
        OperationType type,
        BigDecimal amount,
        Instant timestamp,
        String description) {

    public OperationLog {
        Objects.requireNonNull(accountId, "accountId is required");
        Objects.requireNonNull(type, "type is required");
        Objects.requireNonNull(amount, "amount is required");
        Objects.requireNonNull(timestamp, "timestamp is required");
    }

    public static OperationLog create(String id, String accountId, OperationType type, BigDecimal amount, String description) {
        return new OperationLog(id, accountId, type, amount, Instant.now(), description);
    }

    public OperationLog withId(String newId) {
        return new OperationLog(newId, accountId, type, amount, timestamp, description);
    }
}
