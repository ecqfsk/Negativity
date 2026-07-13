package com.elikill58.negativity.api.violation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Immutable violation log entry exposed through the public API.
 */
public record ViolationRecord(
        @NotNull UUID playerId,
        @NotNull String playerName,
        @NotNull String checkId,
        @NotNull String subcheck,
        double vl,
        double buffer,
        int ping,
        double tps,
        @Nullable String server,
        long timestamp,
        @Nullable String debug
) {
}
