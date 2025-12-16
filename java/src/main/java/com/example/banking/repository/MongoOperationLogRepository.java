package com.example.banking.repository;

import com.example.banking.domain.OperationLog;
import com.example.banking.domain.OperationType;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.conversions.Bson;
import org.bson.types.Binary;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;

public class MongoOperationLogRepository implements OperationLogRepository {
    private final MongoClient client;
    private final MongoCollection<Document> collection;

    public MongoOperationLogRepository(String connectionString, String database, String collectionName) {
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .build();

        this.client = MongoClients.create(settings);
        MongoDatabase db = client.getDatabase(database);
        this.collection = db.getCollection(collectionName);
    }

    @Override
    public OperationLog create(OperationLog log) {
        String id = log.id() != null && !log.id().isBlank() ? log.id() : java.util.UUID.randomUUID().toString();
        Document doc = toDocument(log.withId(id));
        collection.insertOne(doc);
        return log.withId(id);
    }

    @Override
    public Optional<OperationLog> findById(String id) {
        Document doc = collection.find(idFilter(id)).first();
        return Optional.ofNullable(doc).map(this::fromDocument);
    }

    @Override
    public List<OperationLog> findAll() {
        List<OperationLog> logs = new ArrayList<>();
        for (Document doc : collection.find()) {
            logs.add(fromDocument(doc));
        }
        return logs;
    }

    @Override
    public OperationLog update(OperationLog log) {
        if (log.id() == null) {
            throw new RepositoryException("Cannot update log without id");
        }
        Bson filter = idFilter(log.id());
        Document updated = toDocument(log);
        var result = collection.replaceOne(filter, updated);
        if (result.getMatchedCount() == 0) {
            throw new RepositoryException("No operation log found with id " + log.id());
        }
        return log;
    }

    @Override
    public boolean delete(String id) {
        return collection.deleteOne(idFilter(id)).getDeletedCount() > 0;
    }

    @Override
    public void close() {
        client.close();
    }

    private Document toDocument(OperationLog log) {
        return new Document("_id", log.id())
                .append("accountId", log.accountId())
                .append("type", log.type().name())
                .append("amount", log.amount().toString())
                .append("timestamp", Date.from(log.timestamp()))
                .append("description", log.description());
    }

    private OperationLog fromDocument(Document doc) {
        Object rawId = doc.get("_id");
        String id = toIdString(rawId);
        String accountId = doc.getString("accountId");
        OperationType type = OperationType.valueOf(doc.getString("type"));
        BigDecimal amount = new BigDecimal(doc.getString("amount"));
        Date date = doc.getDate("timestamp");
        Instant ts = date.toInstant();
        String description = doc.getString("description");
        return new OperationLog(id, accountId, type, amount, ts, description);
    }

    private Bson idFilter(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            Binary asBinary = new Binary(uuidToBytes(uuid));
            return Filters.or(eq("_id", id), eq("_id", asBinary));
        } catch (IllegalArgumentException e) {
            return eq("_id", id);
        }
    }

    private String toIdString(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof String s) {
            return s;
        }
        if (raw instanceof UUID u) {
            return u.toString();
        }
        if (raw instanceof Binary b) {
            String converted = binaryToString(b);
            if (converted != null) {
                return converted;
            }
        }
        return raw.toString();
    }

    private String binaryToString(Binary b) {
        byte[] data = b.getData();
        if (data != null && data.length == 16) {
            ByteBuffer bb = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);
            long msb = bb.getLong();
            long lsb = bb.getLong();
            return new UUID(msb, lsb).toString();
        }
        return null;
    }

    private byte[] uuidToBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.allocate(16).order(ByteOrder.BIG_ENDIAN);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }
}
