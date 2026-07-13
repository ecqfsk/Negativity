package com.elikill58.negativity.spigot.check;

import com.elikill58.negativity.common.player.PlayerData;
import com.elikill58.negativity.spigot.NegativityPlugin;
import org.bukkit.entity.Player;

public final class FlyCheck {

    private static final String AIR_TICKS = "fly.airTicks";

    private final NegativityPlugin plugin;

    public FlyCheck(NegativityPlugin plugin) {
        this.plugin = plugin;
    }

    public void handle(Player player, PlayerData data, double dy, double tolerance) {
        if (!plugin.checks().isEnabled("fly")) {
            return;
        }
        if (data.isOnGround() || player.isClimbing() || player.isInWater() || player.isSwimming()) {
            data.setCheckData(AIR_TICKS, 0);
            return;
        }
        if (dy < -0.08) {
            data.setCheckData(AIR_TICKS, 0);
            return;
        }

        int airTicks = data.getCheckData(AIR_TICKS, 0) + 1;
        data.setCheckData(AIR_TICKS, airTicks);
        int threshold = (int) Math.round(plugin.config().checkDouble("fly", "air-ticks-threshold", 12) * tolerance);
        if (airTicks < threshold) {
            return;
        }

        boolean hover = Math.abs(dy) < 0.03;
        boolean ascend = dy >= 0.0;
        if (!hover && !ascend) {
            return;
        }

        String sub = hover ? "A" : "B";
        double amount = hover ? 1.2 : 0.9;
        if (airTicks > threshold * 2) {
            amount += 0.8;
        }
        String debug = "airTicks=" + airTicks + " thr=" + threshold
                + " dy=" + String.format(java.util.Locale.ROOT, "%.3f", dy)
                + " hover=" + hover + " tol=" + String.format(java.util.Locale.ROOT, "%.2f", tolerance);
        plugin.checksSupport().flag(player, data, "fly", sub, amount, debug);
        if (airTicks > threshold * 3) {
            data.setCheckData(AIR_TICKS, threshold);
        }
    }
}
