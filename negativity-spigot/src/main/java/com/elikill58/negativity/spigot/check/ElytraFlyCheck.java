package com.elikill58.negativity.spigot.check;

import com.elikill58.negativity.common.player.PlayerData;
import com.elikill58.negativity.spigot.NegativityPlugin;
import org.bukkit.entity.Player;

/**
 * ElytraFly — excessive speed / hover while gliding without firework boost context.
 */
public final class ElytraFlyCheck {

    private static final String BOOST_UNTIL = "elytra.boostUntil";

    private final NegativityPlugin plugin;

    public ElytraFlyCheck(NegativityPlugin plugin) {
        this.plugin = plugin;
    }

    public void markBoost(PlayerData data) {
        data.setCheckData(BOOST_UNTIL, System.currentTimeMillis() + 2500L);
    }

    public void handle(Player player, PlayerData data, double horizontal, double dy) {
        if (!plugin.checks().isEnabled("elytrafly")) {
            return;
        }
        if (!player.isGliding()) {
            return;
        }
        long boostUntil = data.getCheckData(BOOST_UNTIL, 0L);
        if (System.currentTimeMillis() < boostUntil) {
            return;
        }
        double maxH = plugin.config().checkDouble("elytrafly", "max-horizontal", 1.8);
        maxH *= data.latency().movementToleranceMultiplier();
        if (horizontal > maxH) {
            plugin.checksSupport().flag(player, data, "elytrafly", "A",
                    Math.min(4.0, 1.0 + (horizontal - maxH) * 2),
                    "h=" + String.format("%.3f", horizontal) + " max=" + maxH);
        }
        // hover-ish glide
        if (Math.abs(dy) < 0.02 && horizontal < 0.05) {
            plugin.checksSupport().flag(player, data, "elytrafly", "B", 1.2, "hover glide");
        }
    }
}
