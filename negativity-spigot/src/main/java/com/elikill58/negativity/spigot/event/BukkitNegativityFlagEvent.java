package com.elikill58.negativity.spigot.event;

import com.elikill58.negativity.api.event.NegativityFlagEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class BukkitNegativityFlagEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private final NegativityFlagEvent delegate;
    private boolean cancelled;

    public BukkitNegativityFlagEvent(@NotNull NegativityFlagEvent delegate) {
        this.delegate = delegate;
    }

    public NegativityFlagEvent delegate() {
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
