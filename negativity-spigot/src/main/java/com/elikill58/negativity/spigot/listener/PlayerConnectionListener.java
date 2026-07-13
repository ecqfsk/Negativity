package com.elikill58.negativity.spigot.listener;

import com.elikill58.negativity.api.exempt.ExemptReason;
import com.elikill58.negativity.common.player.PlayerData;
import com.elikill58.negativity.spigot.NegativityPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public final class PlayerConnectionListener implements Listener {

    private final NegativityPlugin plugin;

    public PlayerConnectionListener(NegativityPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.players().getOrCreate(player.getUniqueId(), player.getName());
        data.latency().recordPing(player.getPing());
        data.setWorldName(player.getWorld().getName());
        plugin.exemptions().exempt(player.getUniqueId(), ExemptReason.JOIN, plugin.config().joinExemptMs());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.alerts().clear(player);
        plugin.exemptions().removePlayer(player.getUniqueId());
        plugin.players().remove(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        plugin.players().getData(player.getUniqueId()).ifPresent(PlayerData::markTeleport);
        plugin.exemptions().exempt(player.getUniqueId(), ExemptReason.TELEPORT, plugin.config().teleportExemptMs());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        plugin.exemptions().exempt(event.getPlayer().getUniqueId(), ExemptReason.RESPAWN, plugin.config().respawnExemptMs());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        plugin.exemptions().exempt(
                event.getPlayer().getUniqueId(),
                ExemptReason.WORLD_CHANGE,
                plugin.config().worldChangeExemptMs()
        );
        plugin.players().getData(event.getPlayer().getUniqueId()).ifPresent(data ->
                data.setWorldName(event.getPlayer().getWorld().getName()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK
                || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK
                || event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE
                || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
                || event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
            ExemptReason reason = event.getCause().name().contains("EXPLOSION")
                    ? ExemptReason.EXPLOSION
                    : ExemptReason.KNOCKBACK;
            plugin.exemptions().exempt(player.getUniqueId(), reason, plugin.config().knockbackExemptMs());
        }
    }
}
