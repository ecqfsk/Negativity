package com.elikill58.negativity.api.event;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public final class NegativitySetbackEvent {

    private final UUID playerId;
    private final String checkId;
    private boolean cancelled;

    public NegativitySetbackEvent(@NotNull UUID playerId, @NotNull String checkId) {
        this.playerId = Objects.requireNonNull(playerId);
        this.checkId = Objects.requireNonNull(checkId);
    }

    @NotNull
    public UUID playerId() {
        return playerId;
    }

    @NotNull
    public String checkId() {
        return checkId;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
