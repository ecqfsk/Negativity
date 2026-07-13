package com.elikill58.negativity.spigot.check;

import com.elikill58.negativity.common.player.PlayerData;
import com.elikill58.negativity.spigot.NegativityPlugin;
import org.bukkit.entity.Player;

public final class NoWebCheck {

    private final NegativityPlugin plugin;

    public NoWebCheck(NegativityPlugin plugin) {
        this.plugin = plugin;
    }

    public void handle(Player player, PlayerData data, double horizontal) {
        if (!plugin.checks().isEnabled("noweb")) {
            return;
        }
        String block = player.getLocation().getBlock().getType().name();
        if (!(block.contains("WEB") || block.contains("COBWEB"))) {
            return;
        }
        double max = plugin.config().checkDouble("noweb", "max-horizontal", 0.09);
        max *= data.latency().movementToleranceMultiplier();
        if (horizontal > max) {
            plugin.checksSupport().flag(player, data, "noweb", "A",
                    Math.min(3.0, 1.0 + (horizontal - max) * 12),
                    "h=" + String.format("%.3f", horizontal) + " max=" + max);
        }
    }
}
