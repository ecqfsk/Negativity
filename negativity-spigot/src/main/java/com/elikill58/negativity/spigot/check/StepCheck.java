package com.elikill58.negativity.spigot.check;

import com.elikill58.negativity.common.player.PlayerData;
import com.elikill58.negativity.spigot.NegativityPlugin;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Step — impossible vertical step height without jump boost / slab logic.
 */
public final class StepCheck {

    private final NegativityPlugin plugin;

    public StepCheck(NegativityPlugin plugin) {
        this.plugin = plugin;
    }

    public void handle(Player player, PlayerData data, double dy) {
        if (!plugin.checks().isEnabled("step")) {
            return;
        }
        if (!data.isOnGround() && !data.wasOnGround()) {
            return;
        }
        double maxStep = plugin.config().checkDouble("step", "max-step", 0.6);
        PotionEffect jump = player.getPotionEffect(PotionEffectType.JUMP_BOOST);
        if (jump == null) {
            // older API alias
            try {
                jump = player.getPotionEffect(PotionEffectType.getByName("JUMP"));
            } catch (Exception ignored) {
            }
        }
        if (jump != null) {
            maxStep += (jump.getAmplifier() + 1) * 0.5;
        }
        if (dy > maxStep && dy < 1.5 && player.getVelocity().getY() < 0.45) {
            plugin.checksSupport().flag(player, data, "step", "A",
                    Math.min(4.0, 1.0 + (dy - maxStep) * 3.0),
                    "dy=" + String.format("%.3f", dy) + " max=" + maxStep);
        }
    }
}
