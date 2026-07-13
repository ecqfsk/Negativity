package com.elikill58.negativity.spigot.check;

import com.elikill58.negativity.common.player.PlayerData;
import com.elikill58.negativity.spigot.NegativityPlugin;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class SpeedCheck {

    private final NegativityPlugin plugin;

    public SpeedCheck(NegativityPlugin plugin) {
        this.plugin = plugin;
    }

    public void handle(Player player, PlayerData data, double horizontal, double dy, double tolerance) {
        if (!plugin.checks().isEnabled("speed")) {
            return;
        }

        boolean onGround = data.isOnGround();
        double baseMax = onGround
                ? plugin.config().checkDouble("speed", "ground-max", 0.36)
                : plugin.config().checkDouble("speed", "air-max", 0.42);

        float walk = player.getWalkSpeed();
        double speedFactor = walk / 0.2;
        int amplifier = 0;
        PotionEffect speed = player.getPotionEffect(PotionEffectType.SPEED);
        if (speed != null) {
            amplifier = speed.getAmplifier() + 1;
            speedFactor += amplifier * 0.2;
        }
        if (amplifier >= 5) {
            return;
        }

        double max = baseMax * speedFactor * tolerance;
        if (dy > 0.1) {
            max *= 1.08;
        }
        if (horizontal <= max) {
            return;
        }

        double over = horizontal - max;
        double amount = Math.min(4.0, 0.8 + over * 12.0);
        String sub = onGround ? "A" : "B";
        String debug = "h=" + fmt(horizontal) + " max=" + fmt(max)
                + " tol=" + fmt(tolerance) + " walk=" + fmt(walk)
                + " amp=" + amplifier + " dy=" + fmt(dy) + " ground=" + onGround;
        plugin.checksSupport().flag(player, data, "speed", sub, amount, debug);
    }

    private static String fmt(double v) {
        return String.format(java.util.Locale.ROOT, "%.3f", v);
    }
}
