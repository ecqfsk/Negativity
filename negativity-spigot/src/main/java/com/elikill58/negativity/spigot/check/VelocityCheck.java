package com.elikill58.negativity.spigot.check;

import com.elikill58.negativity.common.player.PlayerData;
import com.elikill58.negativity.spigot.NegativityPlugin;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Velocity / AntiKnockback — insufficient horizontal reaction after damage velocity.
 */
public final class VelocityCheck {

    private static final String EXPECT_UNTIL = "velocity.expectUntil";
    private static final String EXPECT_H = "velocity.expectH";
    private static final String SAMPLES = "velocity.badSamples";

    private final NegativityPlugin plugin;

    public VelocityCheck(NegativityPlugin plugin) {
        this.plugin = plugin;
    }

    public void onVelocity(Player player, PlayerData data, Vector velocity) {
        if (!plugin.checks().isEnabled("velocity")) {
            return;
        }
        double h = Math.hypot(velocity.getX(), velocity.getZ());
        if (h < 0.15) {
            return;
        }
        data.markVelocity();
        data.setCheckData(EXPECT_H, h);
        data.setCheckData(EXPECT_UNTIL, System.currentTimeMillis() + 400L + Math.min(300L, data.ping()));
        data.setCheckData(SAMPLES, 0);
    }

    public void onMove(Player player, PlayerData data, double horizontal) {
        if (!plugin.checks().isEnabled("velocity")) {
            return;
        }
        long until = data.getCheckData(EXPECT_UNTIL, 0L);
        if (until == 0L || System.currentTimeMillis() > until) {
            return;
        }
        double expected = data.getCheckData(EXPECT_H, 0.0) * 0.35;
        if (horizontal < expected * 0.25 && !data.isOnGround()) {
            int bad = data.getCheckData(SAMPLES, 0) + 1;
            data.setCheckData(SAMPLES, bad);
            if (bad >= 3) {
                plugin.checksSupport().flag(player, data, "velocity", "A", 1.8,
                        "h=" + String.format("%.3f", horizontal) + " expected~" + String.format("%.3f", expected));
                data.setCheckData(EXPECT_UNTIL, 0L);
            }
        }
    }
}
