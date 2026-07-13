package com.elikill58.negativity.spigot.check;

import com.elikill58.negativity.common.player.PlayerData;
import com.elikill58.negativity.spigot.NegativityPlugin;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

/**
 * Spider — climbing non-climbable walls.
 */
public final class SpiderCheck {

    private static final String STREAK = "spider.streak";

    private final NegativityPlugin plugin;

    public SpiderCheck(NegativityPlugin plugin) {
        this.plugin = plugin;
    }

    public void handle(Player player, PlayerData data, double dy, double horizontal) {
        if (!plugin.checks().isEnabled("spider")) {
            return;
        }
        if (player.isClimbing() || data.isOnGround() || player.isGliding() || player.isInWater()) {
            data.setCheckData(STREAK, 0);
            return;
        }
        boolean wall = isSolid(player.getLocation().getBlock().getRelative(BlockFace.NORTH).getType())
                || isSolid(player.getLocation().getBlock().getRelative(BlockFace.SOUTH).getType())
                || isSolid(player.getLocation().getBlock().getRelative(BlockFace.EAST).getType())
                || isSolid(player.getLocation().getBlock().getRelative(BlockFace.WEST).getType());
        if (wall && dy > 0.12 && horizontal < 0.22) {
            int streak = data.getCheckData(STREAK, 0) + 1;
            data.setCheckData(STREAK, streak);
            if (streak >= 6) {
                plugin.checksSupport().flag(player, data, "spider", "A", 1.4,
                        "streak=" + streak + " dy=" + String.format("%.3f", dy));
            }
        } else {
            data.setCheckData(STREAK, Math.max(0, data.getCheckData(STREAK, 0) - 1));
        }
    }

    private static boolean isSolid(Material m) {
        return m.isSolid() && !m.name().contains("LEAVES");
    }
}
