package com.elikill58.negativity.spigot.check;

import com.elikill58.negativity.common.player.PlayerData;
import com.elikill58.negativity.spigot.NegativityPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * KillAura — multi-target / impossible rotation snaps.
 */
public final class KillAuraCheck {

    private static final String LAST_TARGET = "killaura.lastTarget";
    private static final String SWITCHES = "killaura.switches";
    private static final String SWITCH_WINDOW = "killaura.switchWindow";

    private final NegativityPlugin plugin;

    public KillAuraCheck(NegativityPlugin plugin) {
        this.plugin = plugin;
    }

    public void handle(Player attacker, PlayerData data, Entity target) {
        if (!plugin.checks().isEnabled("killaura")) {
            return;
        }

        // A: looking too far from target
        Location eye = attacker.getEyeLocation();
        Vector center = target.getBoundingBox().getCenter();
        Vector toTarget = center.clone().subtract(eye.toVector());
        if (toTarget.lengthSquared() > 1.0e-6) {
            double angle = eye.getDirection().angle(toTarget.normalize());
            double maxAngle = Math.toRadians(plugin.config().checkDouble("killaura", "max-angle-deg", 70));
            if (angle > maxAngle && eye.distanceSquared(target.getLocation()) > 1.0) {
                plugin.checksSupport().flag(attacker, data, "killaura", "A",
                        Math.min(3.5, 1.0 + (angle - maxAngle) * 2),
                        "angleDeg=" + String.format("%.1f", Math.toDegrees(angle)));
            }
        }

        // B: multi-aura rapid target switching
        String targetId = target.getUniqueId() != null ? target.getUniqueId().toString() : target.getEntityId() + "";
        String last = data.getCheckData(LAST_TARGET, "");
        long now = System.currentTimeMillis();
        long windowStart = data.getCheckData(SWITCH_WINDOW, now);
        if (now - windowStart > 1000L) {
            data.setCheckData(SWITCHES, 0);
            data.setCheckData(SWITCH_WINDOW, now);
        }
        if (!last.isEmpty() && !last.equals(targetId)) {
            int switches = data.getCheckData(SWITCHES, 0) + 1;
            data.setCheckData(SWITCHES, switches);
            if (switches >= 4) {
                plugin.checksSupport().flag(attacker, data, "killaura", "B", 1.5,
                        "switches=" + switches + "/s");
            }
        }
        data.setCheckData(LAST_TARGET, targetId);
    }
}
