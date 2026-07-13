package com.elikill58.negativity.spigot.check;

import com.elikill58.negativity.common.player.PlayerData;
import com.elikill58.negativity.spigot.NegativityPlugin;
import org.bukkit.entity.Player;

/**
 * AirJump — second upward acceleration without leaving ground legitimately.
 */
public final class AirJumpCheck {

    private static final String WAS_FALLING = "airjump.wasFalling";

    private final NegativityPlugin plugin;

    public AirJumpCheck(NegativityPlugin plugin) {
        this.plugin = plugin;
    }

    public void handle(Player player, PlayerData data, double dy) {
        if (!plugin.checks().isEnabled("airjump")) {
            return;
        }
        if (data.isOnGround() || player.isClimbing() || player.isGliding() || player.isInWater()) {
            data.setCheckData(WAS_FALLING, false);
            return;
        }
        boolean wasFalling = data.getCheckData(WAS_FALLING, false);
        if (dy < -0.1) {
            data.setCheckData(WAS_FALLING, true);
            return;
        }
        if (wasFalling && dy > 0.25 && player.getVelocity().getY() > 0.2 && player.getFallDistance() > 0.4f) {
            plugin.checksSupport().flag(player, data, "airjump", "A", 1.6,
                    "dy=" + String.format("%.3f", dy) + " fall=" + player.getFallDistance());
            data.setCheckData(WAS_FALLING, false);
        }
    }
}
