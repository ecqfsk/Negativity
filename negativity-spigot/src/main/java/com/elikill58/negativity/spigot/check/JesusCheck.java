package com.elikill58.negativity.spigot.check;

import com.elikill58.negativity.common.player.PlayerData;
import com.elikill58.negativity.spigot.NegativityPlugin;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Jesus — walking on water/lava surface without sinking.
 */
public final class JesusCheck {

    private static final String STREAK = "jesus.streak";

    private final NegativityPlugin plugin;

    public JesusCheck(NegativityPlugin plugin) {
        this.plugin = plugin;
    }

    public void handle(Player player, PlayerData data, double dy, double horizontal) {
        if (!plugin.checks().isEnabled("jesus")) {
            return;
        }
        if (player.isSwimming() || player.isInWater() || player.getVehicle() != null) {
            data.setCheckData(STREAK, 0);
            return;
        }
        Block below = player.getLocation().clone().subtract(0, 0.3, 0).getBlock();
        Material type = below.getType();
        boolean liquid = type == Material.WATER || type == Material.LAVA
                || type.name().contains("WATER") || type.name().contains("LAVA");
        if (!liquid) {
            data.setCheckData(STREAK, 0);
            return;
        }
        // Standing/walking on liquid with little vertical sink
        if (Math.abs(dy) < 0.05 && horizontal > 0.08 && !player.isFlying()) {
            int streak = data.getCheckData(STREAK, 0) + 1;
            data.setCheckData(STREAK, streak);
            if (streak >= 8) {
                plugin.checksSupport().flag(player, data, "jesus", "A", 1.3,
                        "streak=" + streak + " h=" + String.format("%.3f", horizontal) + " fluid=" + type);
            }
        } else {
            data.setCheckData(STREAK, 0);
        }
    }
}
