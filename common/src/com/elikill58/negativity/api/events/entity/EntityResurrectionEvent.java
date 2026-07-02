package com.elikill58.negativity.api.events.entity;

import com.elikill58.negativity.api.entity.Player;
import com.elikill58.negativity.api.events.CancellableEvent;
import com.elikill58.negativity.api.events.PlayerEvent;

/**
 * Called when a player is saved from death by a totem of undying (resurrection).
 *
 * <p>Fired by every platform that exposes a resurrection/totem hook. It extends
 * {@link PlayerEvent} so it is dispatched to {@code @Check} methods (e.g. AutoTotem).
 * Cancelling it should cancel the resurrection on platforms that support it.
 */
public class EntityResurrectionEvent extends PlayerEvent implements CancellableEvent {

	private boolean cancel = false;

	public EntityResurrectionEvent(Player p) {
		super(p);
	}

	@Override
	public boolean isCancelled() {
		return cancel;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancel = cancel;
	}
}
