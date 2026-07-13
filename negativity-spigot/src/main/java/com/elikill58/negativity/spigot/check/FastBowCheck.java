package com.elikill58.negativity.spigot.check;

import com.elikill58.negativity.common.player.PlayerData;
import com.elikill58.negativity.spigot.NegativityPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class FastBowCheck {

    private static final String LAST_SHOT = "fastbow.last";

    private final NegativityPlugin plugin;

    public FastBowCheck(NegativityPlugin plugin) {
        this.plugin = plugin;
    }

    public void onShoot(Player player, PlayerData data) {
        if (!plugin.checks().isEnabled("fastbow")) {
            return;
        }
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType() != Material.BOW && hand.getType() != Material.CROSSBOW) {
            return;
        }
        long now = System.currentTimeMillis();
        long last = data.getCheckData(LAST_SHOT, 0L);
        data.setCheckData(LAST_SHOT, now);
        if (last == 0L) {
            return;
        }
        long delta = now - last;
        long min = (long) plugin.config().checkDouble("fastbow", "min-interval-ms", 150);
        if (delta < min) {
            plugin.checksSupport().flag(player, data, "fastbow", "A", 1.5,
                    "deltaMs=" + delta + " min=" + min);
        }
    }
}
