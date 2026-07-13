package com.elikill58.negativity.spigot.check;

import com.elikill58.negativity.common.player.PlayerData;
import com.elikill58.negativity.spigot.NegativityPlugin;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

/**
 * NoSlow — moving too fast while using item / blocking / cobweb.
 */
public final class NoSlowCheck {

    private final NegativityPlugin plugin;

    public NoSlowCheck(NegativityPlugin plugin) {
        this.plugin = plugin;
    }

    public void handle(Player player, PlayerData data, double horizontal) {
        if (!plugin.checks().isEnabled("noslow")) {
            return;
        }
        boolean using = player.isHandRaised() || player.getActiveItem() != null && !player.getActiveItem().getType().isAir();
        boolean web = player.getLocation().getBlock().getType().name().contains("WEB")
                || player.getLocation().getBlock().getType().name().contains("COBWEB");
        if (!using && !web) {
            return;
        }
        double max = using ? 0.12 : 0.08;
        max *= data.latency().movementToleranceMultiplier();
        if (player.hasPotionEffect(PotionEffectType.SPEED)) {
            max += 0.05;
        }
        if (horizontal > max + 0.05) {
            plugin.checksSupport().flag(player, data, "noslow", using ? "A" : "B",
                    Math.min(3.5, 1.0 + (horizontal - max) * 10),
                    "h=" + String.format("%.3f", horizontal) + " max=" + max + " using=" + using + " web=" + web);
        }
    }
}
