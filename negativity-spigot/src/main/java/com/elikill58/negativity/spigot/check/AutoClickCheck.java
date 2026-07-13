package com.elikill58.negativity.spigot.check;

import com.elikill58.negativity.common.player.PlayerData;
import com.elikill58.negativity.spigot.NegativityPlugin;
import org.bukkit.entity.Player;

/**
 * AutoClicker — CPS threshold with short history.
 */
public final class AutoClickCheck {

    private final NegativityPlugin plugin;

    public AutoClickCheck(NegativityPlugin plugin) {
        this.plugin = plugin;
    }

    public void onClick(Player player, PlayerData data) {
        if (!plugin.checks().isEnabled("autoclick")) {
            return;
        }
        int cps = data.recordClick();
        int max = (int) plugin.config().checkDouble("autoclick", "max-cps", 18);
        // small ping leniency
        max += Math.min(4, data.ping() / 80);
        if (cps > max) {
            plugin.checksSupport().flag(player, data, "autoclick", "A",
                    Math.min(4.0, 1.0 + (cps - max) * 0.35),
                    "cps=" + cps + " max=" + max);
        }
    }
}
