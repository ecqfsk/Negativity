package com.elikill58.negativity.spigot.check;

import com.elikill58.negativity.common.packet.PacketKind;
import com.elikill58.negativity.common.player.PlayerData;
import com.elikill58.negativity.spigot.NegativityPlugin;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Timer — excessive movement packets per second over a short window.
 */
public final class TimerCheck {

    private static final String SAMPLES = "timer.samples";

    private final NegativityPlugin plugin;

    public TimerCheck(NegativityPlugin plugin) {
        this.plugin = plugin;
    }

    public void handle(Player player, PlayerData data, Map<PacketKind, Integer> snap) {
        if (!plugin.checks().isEnabled("timer")) {
            return;
        }
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        int movement = snap.getOrDefault(PacketKind.FLYING, 0)
                + snap.getOrDefault(PacketKind.POSITION, 0)
                + snap.getOrDefault(PacketKind.LOOK, 0)
                + snap.getOrDefault(PacketKind.POSITION_LOOK, 0);

        @SuppressWarnings("unchecked")
        List<Integer> samples = data.getCheckData(SAMPLES, new ArrayList<Integer>());
        // ensure mutable
        if (!(samples instanceof ArrayList)) {
            samples = new ArrayList<>(samples);
        }
        samples.add(movement);
        int keep = (int) plugin.config().checkDouble("timer", "sample-seconds", 5);
        while (samples.size() > keep) {
            samples.remove(0);
        }
        data.setCheckData(SAMPLES, samples);
        if (samples.size() < Math.min(3, keep)) {
            return;
        }

        double avg = samples.stream().mapToInt(Integer::intValue).average().orElse(0);
        List<Integer> sorted = new ArrayList<>(samples);
        sorted.sort(Comparator.naturalOrder());
        int median = sorted.get(sorted.size() / 2);

        double max = plugin.config().checkDouble("timer", "max-packets-per-second", 22);
        // latency tolerance: allow a few extra packets
        max += Math.min(8, data.ping() / 50.0);
        max *= data.latency().movementToleranceMultiplier();

        if (avg <= max && median <= max) {
            return;
        }

        double over = Math.max(0, avg - max);
        double amount = Math.min(5.0, 1.0 + over * 0.35);
        String debug = "avg=" + String.format("%.1f", avg)
                + " median=" + median
                + " max=" + String.format("%.1f", max)
                + " last=" + movement
                + " samples=" + samples;

        plugin.checksSupport().flag(player, data, "timer", "A", amount, debug);
    }
}
