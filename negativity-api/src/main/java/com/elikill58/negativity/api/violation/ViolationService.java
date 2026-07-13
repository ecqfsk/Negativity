package com.elikill58.negativity.api.violation;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public interface ViolationService {

    double getVl(@NotNull UUID playerId, @NotNull String checkId);

    @NotNull
    List<ViolationRecord> recent(@NotNull UUID playerId, int limit);

    void reset(@NotNull UUID playerId);

    void reset(@NotNull UUID playerId, @NotNull String checkId);
}
