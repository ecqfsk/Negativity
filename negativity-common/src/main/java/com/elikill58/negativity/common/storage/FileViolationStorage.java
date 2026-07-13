package com.elikill58.negativity.common.storage;

import com.elikill58.negativity.api.violation.ViolationRecord;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Async append-only file logs per player UUID.
 */
public final class FileViolationStorage implements ViolationStorage {

    private final Path directory;
    private final Logger logger;
    private ExecutorService executor;

    public FileViolationStorage(@NotNull Path directory, @NotNull Logger logger) {
        this.directory = directory;
        this.logger = logger;
    }

    @Override
    public void start() {
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Cannot create log directory " + directory, e);
        }
        executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "negativity-file-storage");
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public @NotNull CompletableFuture<Void> insertAsync(@NotNull ViolationRecord record) {
        return CompletableFuture.runAsync(() -> {
            Path file = directory.resolve(record.playerId() + ".log");
            String line = Instant.ofEpochMilli(record.timestamp()) + " | "
                    + record.playerName() + " | "
                    + record.checkId() + "/" + record.subcheck()
                    + " | vl=" + record.vl()
                    + " | buffer=" + record.buffer()
                    + " | ping=" + record.ping()
                    + " | tps=" + record.tps()
                    + " | server=" + (record.server() == null ? "-" : record.server())
                    + " | " + (record.debug() == null ? "" : record.debug())
                    + System.lineSeparator();
            try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                w.write(line);
            } catch (IOException e) {
                logger.log(Level.WARNING, "Failed writing violation log", e);
            }
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<List<ViolationRecord>> findRecentAsync(@NotNull UUID playerId, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            Path file = directory.resolve(playerId + ".log");
            if (!Files.isRegularFile(file)) {
                return List.of();
            }
            try {
                List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
                List<ViolationRecord> out = new ArrayList<>();
                int from = Math.max(0, lines.size() - Math.max(1, limit));
                for (int i = lines.size() - 1; i >= from; i--) {
                    // lightweight placeholder records from log lines
                    out.add(new ViolationRecord(
                            playerId, "?", "log", "file", 0, 0, 0, 0, null,
                            System.currentTimeMillis(), lines.get(i)
                    ));
                }
                return Collections.unmodifiableList(out);
            } catch (IOException e) {
                logger.log(Level.WARNING, "Failed reading violation log", e);
                return List.of();
            }
        }, executor);
    }

    @Override
    public void close() {
        if (executor == null) {
            return;
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
