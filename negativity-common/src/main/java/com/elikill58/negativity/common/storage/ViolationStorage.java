package com.elikill58.negativity.common.storage;

import com.elikill58.negativity.api.violation.ViolationRecord;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ViolationStorage extends AutoCloseable {

    void start();

    @NotNull
    CompletableFuture<Void> insertAsync(@NotNull ViolationRecord record);

    @NotNull
    CompletableFuture<List<ViolationRecord>> findRecentAsync(@NotNull UUID playerId, int limit);

    @Override
    void close();
}
