package com.elikill58.negativity.spigot.check;

import com.elikill58.negativity.common.player.PlayerData;
import com.elikill58.negativity.spigot.NegativityPlugin;
import org.bukkit.entity.Player;

/**
 * NoFall — client claims on-ground while falling with significant fall distance.
 */
public final class NoFallCheck {

    private final NegativityPlugin plugin;

    public NoFallCheck(NegativityPlugin plugin) {
        this.plugin = plugin;
    }

    public void handle(Player player, PlayerData data, double dy) {
        if (!plugin.checks().isEnabled("nofall")) {
            return;
        }
        float fall = player.getFallDistance();
        // Falling hard but reports onGround
        if (dy < -0.4 && fall > 3.0f && data.isOnGround() && !player.isGliding() && !player.isInWater()) {
            double amount = Math.min(4.0, 1.0 + (fall - 3.0) * 0.25);
            plugin.checksSupport().flag(player, data, "nofall", "A", amount,
                    "fall=" + fall + " dy=" + String.format("%.3f", dy) + " groundFlag=" + data.isOnGround());
        }
        // Spoofed ground while still airborne and falling
        if (!player.isOnGround() && data.isOnGround() && dy < -0.2 && fall > 2.0f) {
            plugin.checksSupport().flag(player, data, "nofall", "B", 1.2,
                    "bukkitGround=false dataGround=true fall=" + fall);
        }
    }
}
