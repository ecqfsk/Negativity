package com.elikill58.negativity.api.event;

import com.elikill58.negativity.api.violation.ViolationRecord;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class NegativityPunishEvent {

    private final ViolationRecord record;
    private final List<String> commands;
    private boolean cancelled;

    public NegativityPunishEvent(@NotNull ViolationRecord record, @NotNull List<String> commands) {
        this.record = Objects.requireNonNull(record);
        this.commands = new ArrayList<>(Objects.requireNonNull(commands));
    }

    @NotNull
    public ViolationRecord record() {
        return record;
    }

    @NotNull
    public List<String> commands() {
        return commands;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
