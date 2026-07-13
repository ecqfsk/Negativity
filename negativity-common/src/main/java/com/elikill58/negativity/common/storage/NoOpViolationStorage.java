package com.elikill58.negativity.common.storage;

import com.elikill58.negativity.api.violation.ViolationRecord;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class NoOpViolationStorage implements ViolationStorage {
    @Override
    public void start() {
    }

    @Override
    public @NotNull CompletableFuture<Void> insertAsync(@NotNull ViolationRecord record) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public @NotNull CompletableFuture<List<ViolationRecord>> findRecentAsync(@NotNull UUID playerId, int limit) {
        return CompletableFuture.completedFuture(List.of());
    }

    @Override
    public void close() {
    }
}
