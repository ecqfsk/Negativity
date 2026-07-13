package com.elikill58.negativity.spigot.check;

import com.elikill58.negativity.common.player.PlayerData;
import com.elikill58.negativity.spigot.NegativityPlugin;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Phase — player bounding box inside solid blocks while moving.
 */
public final class PhaseCheck {

    private final NegativityPlugin plugin;

    public PhaseCheck(NegativityPlugin plugin) {
        this.plugin = plugin;
    }

    public void handle(Player player, PlayerData data) {
        if (!plugin.checks().isEnabled("phase")) {
            return;
        }
        if (player.getGameMode() == GameMode.SPECTATOR || player.isInsideVehicle()) {
            return;
        }
        Block feet = player.getLocation().getBlock();
        Block head = player.getLocation().clone().add(0, 1.0, 0).getBlock();
        if (isSuffocating(feet.getType()) && isSuffocating(head.getType()) && !player.isSwimming()) {
            plugin.checksSupport().flag(player, data, "phase", "A", 1.8,
                    "feet=" + feet.getType() + " head=" + head.getType());
        }
    }

    private static boolean isSuffocating(Material m) {
        return m.isSolid() && m.isOccluding()
                && !m.name().contains("DOOR")
                && !m.name().contains("SIGN")
                && !m.name().contains("BANNER")
                && !m.name().contains("SLAB")
                && !m.name().contains("STAIR");
    }
}
