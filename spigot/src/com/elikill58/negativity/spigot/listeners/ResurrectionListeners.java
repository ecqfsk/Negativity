package com.elikill58.negativity.spigot.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;

import com.elikill58.negativity.api.events.EventManager;
import com.elikill58.negativity.api.events.entity.EntityResurrectionEvent;
import com.elikill58.negativity.spigot.impl.entity.SpigotEntityManager;

/**
 * Fires {@link EntityResurrectionEvent} for the AutoTotem check.
 *
 * <p>Kept apart from {@link EntityListeners} on purpose: {@link EntityResurrectEvent} only
 * exists since Minecraft 1.11, so registering a handler for it on an older server would throw
 * NoClassDefFoundError. This listener must therefore only be registered when the server is
 * 1.11 or newer.
 */
public class ResurrectionListeners implements Listener {

	@EventHandler
	public void onResurrect(EntityResurrectEvent e) {
		if (!(e.getEntity() instanceof Player))
			return;
		EntityResurrectionEvent event = new EntityResurrectionEvent(SpigotEntityManager.getPlayer((Player) e.getEntity()));
		EventManager.callEvent(event);
		if (event.isCancelled())
			e.setCancelled(true);
	}
}
