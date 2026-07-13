package com.elikill58.negativity.spigot.check;

import com.elikill58.negativity.common.player.PlayerData;
import com.elikill58.negativity.spigot.NegativityPlugin;
import org.bukkit.entity.Player;

public final class FastLadderCheck {

    private final NegativityPlugin plugin;

    public FastLadderCheck(NegativityPlugin plugin) {
        this.plugin = plugin;
    }

    public void handle(Player player, PlayerData data, double dy) {
        if (!plugin.checks().isEnabled("fastladder")) {
            return;
        }
        if (!player.isClimbing()) {
            return;
        }
        double max = plugin.config().checkDouble("fastladder", "max-dy", 0.15);
        max *= data.latency().movementToleranceMultiplier();
        if (dy > max) {
            plugin.checksSupport().flag(player, data, "fastladder", "A",
                    Math.min(3.5, 1.0 + (dy - max) * 10),
                    "dy=" + String.format("%.3f", dy) + " max=" + max);
        }
    }
}
