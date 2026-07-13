package com.elikill58.negativity.api.event;

import com.elikill58.negativity.api.exempt.ExemptReason;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public final class NegativityExemptEvent {

    private final UUID playerId;
    private final String checkId;
    private final ExemptReason reason;
    private final long durationMs;
    private boolean cancelled;

    public NegativityExemptEvent(
            @NotNull UUID playerId,
            @NotNull String checkId,
            @NotNull ExemptReason reason,
            long durationMs
    ) {
        this.playerId = Objects.requireNonNull(playerId);
        this.checkId = Objects.requireNonNull(checkId);
        this.reason = Objects.requireNonNull(reason);
        this.durationMs = durationMs;
    }

    @NotNull
    public UUID playerId() {
        return playerId;
    }

    @NotNull
    public String checkId() {
        return checkId;
    }

    @NotNull
    public ExemptReason reason() {
        return reason;
    }

    public long durationMs() {
        return durationMs;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
