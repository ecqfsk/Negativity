package com.elikill58.negativity.spigot.check;

import com.elikill58.negativity.common.math.Aabb;
import com.elikill58.negativity.common.player.PlayerData;
import com.elikill58.negativity.spigot.NegativityPlugin;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

/**
 * Reach — AABB edge distance beyond creative/survival limits with latency slack.
 */
public final class ReachCheck {

    private final NegativityPlugin plugin;

    public ReachCheck(NegativityPlugin plugin) {
        this.plugin = plugin;
    }

    public void handle(Player attacker, PlayerData data, Entity target) {
        if (!plugin.checks().isEnabled("reach")) {
            return;
        }
        if (attacker.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        double max = plugin.config().checkDouble("reach", "max-distance", 3.15);
        max += Math.min(0.6, data.ping() / 250.0);
        max *= Math.max(1.0, data.latency().movementToleranceMultiplier() * 0.95);

        BoundingBox a = attacker.getBoundingBox();
        BoundingBox b = target.getBoundingBox();
        double dist = Aabb.distance(
                new Aabb(a.getMinX(), a.getMinY(), a.getMinZ(), a.getMaxX(), a.getMaxY(), a.getMaxZ()),
                new Aabb(b.getMinX(), b.getMinY(), b.getMinZ(), b.getMaxX(), b.getMaxY(), b.getMaxZ())
        );
        if (dist > max) {
            double amount = Math.min(5.0, 1.0 + (dist - max) * 2.5);
            plugin.checksSupport().flag(attacker, data, "reach", "A", amount,
                    "dist=" + String.format("%.3f", dist) + " max=" + String.format("%.3f", max)
                            + " target=" + target.getType());
        }
    }
}

