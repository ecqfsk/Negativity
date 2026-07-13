package com.elikill58.negativity.spigot.check;

import com.elikill58.negativity.common.player.PlayerData;
import com.elikill58.negativity.spigot.NegativityPlugin;
import org.bukkit.entity.Player;

public final class RegenCheck {

    private static final String LAST = "regen.last";

    private final NegativityPlugin plugin;

    public RegenCheck(NegativityPlugin plugin) {
        this.plugin = plugin;
    }

    public void onRegen(Player player, PlayerData data) {
        if (!plugin.checks().isEnabled("regen")) {
            return;
        }
        long now = System.currentTimeMillis();
        long last = data.getCheckData(LAST, 0L);
        data.setCheckData(LAST, now);
        if (last == 0L) {
            return;
        }
        long delta = now - last;
        // vanilla peaceful/hardcore varies; use conservative floor with ping
        long min = (long) plugin.config().checkDouble("regen", "min-interval-ms", 450);
        min = Math.max(250, min - Math.min(150, data.ping() / 2));
        if (delta < min && player.getFoodLevel() < 18) {
            plugin.checksSupport().flag(player, data, "regen", "A", 1.3,
                    "deltaMs=" + delta + " min=" + min + " food=" + player.getFoodLevel());
        }
    }
}
