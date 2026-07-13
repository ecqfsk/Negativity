package com.elikill58.negativity.spigot.event;

import com.elikill58.negativity.api.event.NegativityPunishEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class BukkitNegativityPunishEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private final NegativityPunishEvent delegate;
    private boolean cancelled;

    public BukkitNegativityPunishEvent(@NotNull NegativityPunishEvent delegate) {
        this.delegate = delegate;
    }

    public NegativityPunishEvent delegate() {
        return delegate;
    }

    @Override
    public boolean isCancelled() {
        return cancelled || delegate.isCancelled();
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
        delegate.setCancelled(cancel);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
