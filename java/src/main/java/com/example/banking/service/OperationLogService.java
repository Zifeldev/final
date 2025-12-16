package com.example.banking.service;

import com.example.banking.domain.OperationLog;
import com.example.banking.domain.OperationType;
import com.example.banking.repository.OperationLogRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class OperationLogService implements AutoCloseable {
    private final OperationLogRepository repository;

    public OperationLogService(OperationLogRepository repository) {
        this.repository = repository;
    }

    public OperationLog logOperation(String id, String accountId, OperationType type, BigDecimal amount, String description) {
        OperationLog log = new OperationLog(id, accountId, type, amount, Instant.now(), description);
        return repository.create(log);
    }

    public Optional<OperationLog> find(String id) {
        return repository.findById(id);
    }

    public List<OperationLog> listAll() {
        return repository.findAll();
    }

    public OperationLog update(OperationLog log) {
        return repository.update(log);
    }

    public boolean remove(String id) {
        return repository.delete(id);
    }

    @Override
    public void close() {
        repository.close();
    }
}
