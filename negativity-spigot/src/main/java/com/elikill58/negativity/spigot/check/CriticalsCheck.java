package com.elikill58.negativity.spigot.check;

import com.elikill58.negativity.common.player.PlayerData;
import com.elikill58.negativity.spigot.NegativityPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Criticals — critical hits while on ground / impossible fall state.
 */
public final class CriticalsCheck {

    private final NegativityPlugin plugin;

    public CriticalsCheck(NegativityPlugin plugin) {
        this.plugin = plugin;
    }

    public void handle(Player attacker, PlayerData data, EntityDamageByEntityEvent event) {
        if (!plugin.checks().isEnabled("criticals")) {
            return;
        }
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            return;
        }
        // Paper exposes critical via damage modifier sometimes; approximate:
        boolean looksCritical = !attacker.isOnGround()
                && attacker.getFallDistance() > 0
                && !attacker.isInsideVehicle()
                && !attacker.isClimbing()
                && attacker.getVelocity().getY() < 0;
        // Impossible: critical-like state while clearly grounded and not falling
        if (attacker.isOnGround() && data.isOnGround() && attacker.getFallDistance() == 0
                && attacker.getVelocity().getY() >= -0.08
                && event.getDamage() > 1.0
                && looksCritical) {
            // contradictory — skip
            return;
        }
        // Flag clients that claim air crit but server ground is true and fall is 0
        if (data.isOnGround() && attacker.getFallDistance() <= 0.0001f
                && !attacker.isInWater()
                && attacker.getVelocity().getY() > -0.05
                && event.getFinalDamage() > event.getDamage()) {
            // not reliable alone — use softer signal
        }
        // Simpler reliable-ish: attack while fallDistance==0 and packet ground false and y vel ~0 for too many
        if (!data.isOnGround() && attacker.isOnGround() && attacker.getFallDistance() == 0.0f
                && Math.abs(attacker.getVelocity().getY()) < 0.03) {
            plugin.checksSupport().flag(attacker, data, "criticals", "A", 1.1,
                    "ground mismatch during attack");
        }
    }
}
