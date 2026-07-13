package com.elikill58.negativity.spigot.check;

import com.elikill58.negativity.common.player.PlayerData;
import com.elikill58.negativity.spigot.NegativityPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

/**
 * InventoryMove — significant sprint-like movement with open inventory.
 */
public final class InventoryMoveCheck {

    private final NegativityPlugin plugin;

    public InventoryMoveCheck(NegativityPlugin plugin) {
        this.plugin = plugin;
    }

    public void handle(Player player, PlayerData data, double horizontal) {
        if (!plugin.checks().isEnabled("inventorymove")) {
            return;
        }
        if (player.getOpenInventory() == null) {
            return;
        }
        InventoryType type = player.getOpenInventory().getType();
        if (type == InventoryType.CRAFTING || type == InventoryType.CREATIVE) {
            return;
        }
        double max = plugin.config().checkDouble("inventorymove", "max-horizontal", 0.15);
        max *= data.latency().movementToleranceMultiplier();
        if (horizontal > max && (player.isSprinting() || horizontal > max * 1.4)) {
            plugin.checksSupport().flag(player, data, "inventorymove", "A", 1.2,
                    "h=" + String.format("%.3f", horizontal) + " inv=" + type);
        }
    }
}
