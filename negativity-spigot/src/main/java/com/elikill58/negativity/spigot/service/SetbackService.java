package com.elikill58.negativity.spigot.service;

import com.elikill58.negativity.common.player.PlayerData;
import com.elikill58.negativity.spigot.NegativityPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Safe setback using last known valid position.
 */
public final class SetbackService {

    private final NegativityPlugin plugin;

    public SetbackService(NegativityPlugin plugin) {
        this.plugin = plugin;
    }

    public void recordIfSafe(Player player, PlayerData data) {
        Location loc = player.getLocation();
        if (!isSafe(loc)) {
            return;
        }
        // Standing-ish on ground and not inside solid
        if (!player.isInsideVehicle() && (player.isOnGround() || data.isOnGround())) {
            data.setLastSafePosition(new PlayerData.SafePosition(
                    loc.getWorld().getName(),
                    loc.getX(),
                    loc.getY(),
                    loc.getZ(),
                    loc.getYaw(),
                    loc.getPitch()
            ));
        }
    }

    public boolean setback(Player player, PlayerData data) {
        PlayerData.SafePosition safe = data.lastSafePosition();
        if (safe == null) {
            return false;
        }
        World world = Bukkit.getWorld(safe.world());
        if (world == null) {
            return false;
        }
        Location target = new Location(world, safe.x(), safe.y(), safe.z(), safe.yaw(), safe.pitch());
        if (!isSafe(target)) {
            // try one block up
            target.add(0, 1, 0);
            if (!isSafe(target)) {
                return false;
            }
        }
        // Avoid setback loops: temporary teleport exemption
        plugin.exemptions().exempt(
                player.getUniqueId(),
                com.elikill58.negativity.api.exempt.ExemptReason.TELEPORT,
                plugin.config().teleportExemptMs(),
                "setback"
        );
        player.teleport(target);
        return true;
    }

    private boolean isSafe(Location loc) {
        if (loc.getWorld() == null) {
            return false;
        }
        Block feet = loc.getBlock();
        Block head = loc.clone().add(0, 1, 0).getBlock();
        Material feetType = feet.getType();
        Material headType = head.getType();
        if (feetType.isSolid() || headType.isSolid()) {
            return false;
        }
        // don't setback into lava
        return feetType != Material.LAVA && headType != Material.LAVA;
    }
}
