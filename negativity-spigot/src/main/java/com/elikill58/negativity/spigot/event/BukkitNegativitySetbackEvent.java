package com.elikill58.negativity.spigot.event;

import com.elikill58.negativity.api.event.NegativitySetbackEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class BukkitNegativitySetbackEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private final NegativitySetbackEvent delegate;
    private boolean cancelled;

    public BukkitNegativitySetbackEvent(@NotNull NegativitySetbackEvent delegate) {
        this.delegate = delegate;
    }

    public NegativitySetbackEvent delegate() {
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
