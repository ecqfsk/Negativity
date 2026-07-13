package com.elikill58.negativity.spigot.check;

import com.elikill58.negativity.common.player.PlayerData;
import com.elikill58.negativity.spigot.NegativityPlugin;
import org.bukkit.entity.Player;

public final class FastPlaceCheck {

    private static final String COUNT = "fastplace.count";
    private static final String WINDOW = "fastplace.window";

    private final NegativityPlugin plugin;

    public FastPlaceCheck(NegativityPlugin plugin) {
        this.plugin = plugin;
    }

    public void onPlace(Player player, PlayerData data) {
        if (!plugin.checks().isEnabled("fastplace")) {
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
        int max = (int) plugin.config().checkDouble("fastplace", "max-pps", 14);
        if (count > max) {
            plugin.checksSupport().flag(player, data, "fastplace", "A",
                    Math.min(4.0, 1.0 + (count - max) * 0.35),
                    "places=" + count + "/s");
        }
    }
}
