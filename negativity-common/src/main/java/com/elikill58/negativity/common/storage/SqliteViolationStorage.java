package com.elikill58.negativity.common.storage;

import com.elikill58.negativity.api.violation.ViolationRecord;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Async SQLite violation storage with batched single-thread access.
 */
public final class SqliteViolationStorage implements ViolationStorage {

    private final Path dbFile;
    private final Logger logger;
    private ExecutorService executor;
    private Connection connection;

    public SqliteViolationStorage(@NotNull Path dbFile, @NotNull Logger logger) {
        this.dbFile = dbFile;
        this.logger = logger;
    }

    @Override
    public void start() {
        executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "negativity-sqlite");
            t.setDaemon(true);
            return t;
        });
        executor.execute(() -> {
            try {
                Class.forName("org.sqlite.JDBC");
                if (dbFile.getParent() != null) {
                    java.nio.file.Files.createDirectories(dbFile.getParent());
                }
                connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.toAbsolutePath());
                try (Statement st = connection.createStatement()) {
                    st.executeUpdate("""
                            CREATE TABLE IF NOT EXISTS violations (
                              id INTEGER PRIMARY KEY AUTOINCREMENT,
                              uuid TEXT NOT NULL,
                              name TEXT NOT NULL,
                              check_id TEXT NOT NULL,
                              subcheck TEXT NOT NULL,
                              vl REAL NOT NULL,
                              buffer REAL NOT NULL,
                              ping INTEGER NOT NULL,
                              tps REAL NOT NULL,
                              server TEXT,
                              ts INTEGER NOT NULL,
                              debug TEXT
                            )
                            """);
                    st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_violations_uuid_ts ON violations(uuid, ts DESC)");
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to init SQLite storage", e);
            }
        });
    }

    @Override
    public @NotNull CompletableFuture<Void> insertAsync(@NotNull ViolationRecord record) {
        return CompletableFuture.runAsync(() -> {
            if (connection == null) {
                return;
            }
            String sql = """
                    INSERT INTO violations(uuid,name,check_id,subcheck,vl,buffer,ping,tps,server,ts,debug)
                    VALUES(?,?,?,?,?,?,?,?,?,?,?)
                    """;
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, record.playerId().toString());
                ps.setString(2, sanitize(record.playerName()));
                ps.setString(3, sanitize(record.checkId()));
                ps.setString(4, sanitize(record.subcheck()));
                ps.setDouble(5, record.vl());
                ps.setDouble(6, record.buffer());
                ps.setInt(7, record.ping());
                ps.setDouble(8, record.tps());
                ps.setString(9, record.server());
                ps.setLong(10, record.timestamp());
                ps.setString(11, record.debug() == null ? null : sanitize(record.debug()));
                ps.executeUpdate();
            } catch (SQLException e) {
                logger.log(Level.WARNING, "SQLite insert failed", e);
            }
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<List<ViolationRecord>> findRecentAsync(@NotNull UUID playerId, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            List<ViolationRecord> out = new ArrayList<>();
            if (connection == null) {
                return out;
            }
            String sql = """
                    SELECT uuid,name,check_id,subcheck,vl,buffer,ping,tps,server,ts,debug
                    FROM violations WHERE uuid=? ORDER BY ts DESC LIMIT ?
                    """;
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, playerId.toString());
                ps.setInt(2, Math.max(1, limit));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        out.add(new ViolationRecord(
                                UUID.fromString(rs.getString(1)),
                                rs.getString(2),
                                rs.getString(3),
                                rs.getString(4),
                                rs.getDouble(5),
                                rs.getDouble(6),
                                rs.getInt(7),
                                rs.getDouble(8),
                                rs.getString(9),
                                rs.getLong(10),
                                rs.getString(11)
                        ));
                    }
                }
            } catch (SQLException e) {
                logger.log(Level.WARNING, "SQLite query failed", e);
            }
            return out;
        }, executor);
    }

    private static String sanitize(String s) {
        if (s == null) {
            return "";
        }
        // strip control chars for log/db safety
        String cleaned = s.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
        return cleaned.substring(0, Math.min(cleaned.length(), 512));
    }

    @Override
    public void close() {
        if (executor != null) {
            executor.execute(() -> {
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    logger.log(Level.WARNING, "SQLite close failed", e);
                }
            });
            executor.shutdown();
            try {
                if (!executor.awaitTermination(8, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
