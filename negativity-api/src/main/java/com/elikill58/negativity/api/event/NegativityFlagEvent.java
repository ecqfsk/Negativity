package com.elikill58.negativity.api.event;

import com.elikill58.negativity.api.violation.ViolationRecord;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

/**
 * Fired when a check produces a violation record (before alert/setback/punish).
 * Platform adapters bridge this to Bukkit/Bungee events.
 */
public final class NegativityFlagEvent {

    private final ViolationRecord record;
    private boolean cancelled;

    public NegativityFlagEvent(@NotNull ViolationRecord record) {
        this.record = Objects.requireNonNull(record);
    }

    @NotNull
    public ViolationRecord record() {
        return record;
    }

    @NotNull
    public UUID playerId() {
        return record.playerId();
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
