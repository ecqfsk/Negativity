package com.elikill58.negativity.api.player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Immutable-friendly view of a tracked player for the public API.
 */
public interface PlayerSnapshot {

    @NotNull
    UUID uuid();

    @NotNull
    String name();

    int ping();

    double jitter();

    boolean isAnalyzed();

    boolean isBedrock();

    @Nullable
    String clientBrand();

    @Nullable
    String worldName();

    double violationLevel(@NotNull String checkId);
}
