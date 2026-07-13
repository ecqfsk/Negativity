package com.elikill58.negativity.spigot.check;

import com.elikill58.negativity.common.player.PlayerData;
import com.elikill58.negativity.spigot.NegativityPlugin;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

/**
 * Nuker / FastBreak — too many block breaks per second.
 */
public final class NukerCheck {

    private static final String COUNT = "nuker.count";
    private static final String WINDOW = "nuker.window";

    private final NegativityPlugin plugin;

    public NukerCheck(NegativityPlugin plugin) {
        this.plugin = plugin;
    }

    public void onBreak(Player player, PlayerData data) {
        if (!plugin.checks().isEnabled("nuker")) {
            return;
        }
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        long now = System.currentTimeMillis();
        long window = data.getCheckData(WINDOW, now);
        if (now - window > 1000L) {
            data.setCheckData(WINDOW, now);
            data.setCheckData(COUNT, 0);
        }
        int count = data.getCheckData(COUNT, 0) + 1;
        data.setCheckData(COUNT, count);
        int max = (int) plugin.config().checkDouble("nuker", "max-bps", 12);
        if (count > max) {
            plugin.checksSupport().flag(player, data, "nuker", "A",
                    Math.min(5.0, 1.0 + (count - max) * 0.4),
                    "breaks=" + count + "/s max=" + max);
        }
    }
}
