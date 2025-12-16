package com.example.banking.repository;

import com.example.banking.domain.OperationLog;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryOperationLogRepository implements OperationLogRepository {
    private final Map<String, OperationLog> storage = new ConcurrentHashMap<>();

    @Override
    public OperationLog create(OperationLog log) {
        String id = log.id() != null && !log.id().isBlank() ? log.id() : UUID.randomUUID().toString();
        OperationLog saved = log.withId(id);
        storage.put(id, saved);
        return saved;
    }

    @Override
    public Optional<OperationLog> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<OperationLog> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public OperationLog update(OperationLog log) {
        if (log.id() == null || !storage.containsKey(log.id())) {
            throw new RepositoryException("Cannot update missing OperationLog with id " + log.id());
        }
        storage.put(log.id(), log);
        return log;
    }

    @Override
    public boolean delete(String id) {
        return storage.remove(id) != null;
    }
}
