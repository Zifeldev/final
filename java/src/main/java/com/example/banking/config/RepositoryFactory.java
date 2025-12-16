package com.example.banking.config;

import com.example.banking.repository.InMemoryOperationLogRepository;
import com.example.banking.repository.MongoOperationLogRepository;
import com.example.banking.repository.OperationLogRepository;
import com.example.banking.repository.PostgresOperationLogRepository;
import io.github.cdimascio.dotenv.Dotenv;

public final class RepositoryFactory {
    private static final Dotenv DOTENV = Dotenv.configure()
            .filename(".env")
            .ignoreIfMissing()
            .load();

    private RepositoryFactory() {
    }

    public static OperationLogRepository create() {
        RepositoryType type = RepositoryType.from(resolve("REPOSITORY_TYPE", "repo.type"));
        return switch (type) {
            case POSTGRES -> createPostgres();
            case MONGO -> createMongo();
            case IN_MEMORY -> new InMemoryOperationLogRepository();
        };
    }

    private static OperationLogRepository createPostgres() {
        String url = required("POSTGRES_URL", "db.postgres.url");
        String user = required("POSTGRES_USER", "db.postgres.user");
        String pass = required("POSTGRES_PASSWORD", "db.postgres.password");
        return new PostgresOperationLogRepository(url, user, pass);
    }

    private static OperationLogRepository createMongo() {
        String uri = required("MONGO_URI", "db.mongo.uri");
        String db = resolve("MONGO_DB", "db.mongo.database", "banking");
        String collection = resolve("MONGO_COLLECTION", "db.mongo.collection", "operationLogs");
        return new MongoOperationLogRepository(uri, db, collection);
    }

    private static String resolve(String envKey, String propertyKey) {
        return resolve(envKey, propertyKey, null);
    }

    private static String resolve(String envKey, String propertyKey, String defaultValue) {
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }

        String propValue = System.getProperty(propertyKey);
        if (propValue != null && !propValue.isBlank()) {
            return propValue;
        }

        String dotenvValue = DOTENV.get(envKey);
        if (dotenvValue != null && !dotenvValue.isBlank()) {
            return dotenvValue;
        }

        return defaultValue;
    }

    private static String required(String envKey, String propertyKey) {
        String value = resolve(envKey, propertyKey, null);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required config for " + envKey + " or system property " + propertyKey);
        }
        return value;
    }
}
