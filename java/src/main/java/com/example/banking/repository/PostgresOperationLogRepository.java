package com.example.banking.repository;

import com.example.banking.domain.OperationLog;
import com.example.banking.domain.OperationType;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PostgresOperationLogRepository implements OperationLogRepository {
    private final String url;
    private final String user;
    private final String password;

    public PostgresOperationLogRepository(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    @Override
    public OperationLog create(OperationLog log) {
        String id = log.id() != null && !log.id().isBlank() ? log.id() : UUID.randomUUID().toString();
        String sql = "INSERT INTO operation_logs(id, account_id, type, amount, ts, description) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.setString(2, log.accountId());
            stmt.setString(3, log.type().name());
            stmt.setBigDecimal(4, log.amount());
            stmt.setTimestamp(5, Timestamp.from(log.timestamp()));
            stmt.setString(6, log.description());
            stmt.executeUpdate();
            return log.withId(id);
        } catch (SQLException e) {
            throw new RepositoryException("Failed to insert operation log", e);
        }
    }

    @Override
    public Optional<OperationLog> findById(String id) {
        String sql = "SELECT id, account_id, type, amount, ts, description FROM operation_logs WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to fetch operation log", e);
        }
    }

    @Override
    public List<OperationLog> findAll() {
        String sql = "SELECT id, account_id, type, amount, ts, description FROM operation_logs ORDER BY ts DESC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            List<OperationLog> result = new ArrayList<>();
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to fetch operation logs", e);
        }
    }

    @Override
    public OperationLog update(OperationLog log) {
        if (log.id() == null) {
            throw new RepositoryException("Cannot update log without id");
        }
        String sql = "UPDATE operation_logs SET account_id = ?, type = ?, amount = ?, ts = ?, description = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, log.accountId());
            stmt.setString(2, log.type().name());
            stmt.setBigDecimal(3, log.amount());
            stmt.setTimestamp(4, Timestamp.from(log.timestamp()));
            stmt.setString(5, log.description());
            stmt.setString(6, log.id());
            int updated = stmt.executeUpdate();
            if (updated == 0) {
                throw new RepositoryException("No operation log found with id " + log.id());
            }
            return log;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to update operation log", e);
        }
    }

    @Override
    public boolean delete(String id) {
        String sql = "DELETE FROM operation_logs WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete operation log", e);
        }
    }

    private OperationLog mapRow(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String accountId = rs.getString("account_id");
        OperationType type = OperationType.valueOf(rs.getString("type"));
        BigDecimal amount = rs.getBigDecimal("amount");
        Instant ts = rs.getTimestamp("ts").toInstant();
        String description = rs.getString("description");
        return new OperationLog(id, accountId, type, amount, ts, description);
    }
}
