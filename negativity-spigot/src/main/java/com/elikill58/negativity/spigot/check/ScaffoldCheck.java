package com.elikill58.negativity.spigot.check;

import com.elikill58.negativity.common.player.PlayerData;
import com.elikill58.negativity.spigot.NegativityPlugin;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Scaffold — placing bridge blocks while looking away / under feet too fast.
 */
public final class ScaffoldCheck {

    private final NegativityPlugin plugin;

    public ScaffoldCheck(NegativityPlugin plugin) {
        this.plugin = plugin;
    }

    public void onPlace(Player player, PlayerData data, Block placed) {
        if (!plugin.checks().isEnabled("scaffold")) {
            return;
        }
        Location eye = player.getEyeLocation();
        Vector toBlock = placed.getLocation().add(0.5, 0.5, 0.5).toVector().subtract(eye.toVector());
        if (toBlock.lengthSquared() < 1.0e-4) {
            return;
        }
        double angle = eye.getDirection().angle(toBlock.normalize());
        boolean under = placed.getY() + 1.05 >= player.getLocation().getY()
                && placed.getLocation().distanceSquared(player.getLocation()) < 6.0;
        if (under && angle > Math.toRadians(90) && player.getLocation().getPitch() < 50) {
            plugin.checksSupport().flag(player, data, "scaffold", "A", 1.4,
                    "angle=" + String.format("%.1f", Math.toDegrees(angle))
                            + " pitch=" + player.getLocation().getPitch());
        }
        // tower: rapid upward place under player
        if (under && Math.abs(placed.getX() + 0.5 - player.getLocation().getX()) < 0.8
                && Math.abs(placed.getZ() + 0.5 - player.getLocation().getZ()) < 0.8
                && placed.getY() >= player.getLocation().getBlockY() - 1
                && player.getVelocity().getY() > 0.3) {
            plugin.checksSupport().flag(player, data, "scaffold", "B", 1.1, "tower-like place");
        }
    }
}
