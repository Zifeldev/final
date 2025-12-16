package com.example.banking.repository;

import com.example.banking.domain.OperationLog;
import java.util.List;
import java.util.Optional;

public interface OperationLogRepository extends AutoCloseable {
    OperationLog create(OperationLog log);
    Optional<OperationLog> findById(String id);
    List<OperationLog> findAll();
    OperationLog update(OperationLog log);
    boolean delete(String id);

    @Override
    default void close() {
        // default no-op
    }
}
