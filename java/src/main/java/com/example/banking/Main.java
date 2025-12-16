package com.example.banking;

import com.example.banking.config.RepositoryFactory;
import com.example.banking.domain.OperationLog;
import com.example.banking.domain.OperationType;
import com.example.banking.repository.OperationLogRepository;
import com.example.banking.service.OperationLogService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        OperationLogRepository repository = RepositoryFactory.create();
        try (OperationLogService service = new OperationLogService(repository);
             Scanner scanner = new Scanner(System.in)) {
            System.out.println("Operation Log CLI. Type 'help' for commands, 'exit' to quit.");
            boolean running = true;
            while (running) {
                System.out.print("> ");
                if (!scanner.hasNextLine()) {
                    break;
                }
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] parts = line.split("\\s+", 2);
                String cmd = parts[0].toLowerCase(Locale.ROOT);
                switch (cmd) {
                    case "help" -> printHelp();
                    case "add" -> handleAdd(scanner, service);
                    case "list" -> handleList(service);
                    case "get" -> handleGet(parts, service);
                    case "delete" -> handleDelete(parts, service);
                    case "update" -> handleUpdate(parts, scanner, service);
                    case "exit", "quit" -> running = false;
                    default -> System.out.println("Unknown command. Type 'help'.");
                }
            }
        }
    }

    private static void printHelp() {
        System.out.println("Commands:");
        System.out.println("  add                 - create a new operation log");
        System.out.println("  list                - list all logs");
        System.out.println("  get <id>            - fetch log by id");
        System.out.println("  update <id>         - update amount/description");
        System.out.println("  delete <id>         - delete log by id");
        System.out.println("  help                - show this help");
        System.out.println("  exit | quit         - exit the program");
    }

    private static void handleAdd(Scanner scanner, OperationLogService service) {
        String id = prompt(scanner, "Id (leave blank for auto): ");
        String accountId = prompt(scanner, "Account ID: ");
        OperationType type = promptType(scanner);
        BigDecimal amount = promptAmount(scanner);
        String description = prompt(scanner, "Description: ");
        OperationLog created = service.logOperation(id.isBlank() ? null : id, accountId, type, amount, description);
        System.out.println("Created: " + created);
    }

    private static void handleList(OperationLogService service) {
        List<OperationLog> logs = service.listAll();
        if (logs.isEmpty()) {
            System.out.println("No logs found.");
            return;
        }
        logs.forEach(log -> System.out.println("- " + log));
    }

    private static void handleGet(String[] parts, OperationLogService service) {
        Optional<String> id = parseId(parts);
        if (id.isEmpty()) {
            return;
        }
        Optional<OperationLog> log = service.find(id.get());
        System.out.println(log.map(Object::toString).orElse("Not found"));
    }

    private static void handleDelete(String[] parts, OperationLogService service) {
        Optional<String> id = parseId(parts);
        if (id.isEmpty()) {
            return;
        }
        boolean removed = service.remove(id.get());
        System.out.println(removed ? "Deleted." : "Not found.");
    }

    private static void handleUpdate(String[] parts, Scanner scanner, OperationLogService service) {
        Optional<String> idOpt = parseId(parts);
        if (idOpt.isEmpty()) {
            return;
        }
        String id = idOpt.get();
        Optional<OperationLog> existing = service.find(id);
        if (existing.isEmpty()) {
            System.out.println("Not found.");
            return;
        }
        OperationLog current = existing.get();
        BigDecimal amount = promptAmount(scanner, "Amount (current: " + current.amount() + "): ");
        String description = prompt(scanner, "Description (current: " + current.description() + "): ");
        OperationLog updated = new OperationLog(
                current.id(),
                current.accountId(),
                current.type(),
                amount,
                current.timestamp(),
                description.isBlank() ? current.description() : description
        );
        service.update(updated);
        System.out.println("Updated: " + updated);
    }

    private static Optional<String> parseId(String[] parts) {
        if (parts.length < 2) {
            System.out.println("Usage requires an id.");
            return Optional.empty();
        }
        String id = parts[1].trim();
        if (id.isEmpty()) {
            System.out.println("Invalid id.");
            return Optional.empty();
        }
        return Optional.of(id);
    }

    private static OperationType promptType(Scanner scanner) {
        while (true) {
            String raw = prompt(scanner, "Type (DEPOSIT/WITHDRAWAL/TRANSFER): ").toUpperCase(Locale.ROOT);
            try {
                return OperationType.valueOf(raw);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid type, try again.");
            }
        }
    }

    private static BigDecimal promptAmount(Scanner scanner) {
        return promptAmount(scanner, "Amount: ");
    }

    private static BigDecimal promptAmount(Scanner scanner, String label) {
        while (true) {
            String raw = prompt(scanner, label);
            try {
                return new BigDecimal(raw);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number, try again.");
            }
        }
    }

    private static String prompt(Scanner scanner, String label) {
        System.out.print(label);
        String line = scanner.nextLine();
        return line == null ? "" : line.trim();
    }
}
