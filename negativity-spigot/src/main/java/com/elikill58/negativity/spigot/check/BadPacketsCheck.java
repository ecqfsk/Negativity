package com.elikill58.negativity.spigot.check;

import com.elikill58.negativity.common.packet.PacketKind;
import com.elikill58.negativity.common.player.PlayerData;
import com.elikill58.negativity.spigot.NegativityPlugin;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * BadPackets — impossible sequences / spam of non-movement packets.
 */
public final class BadPacketsCheck {

    private final NegativityPlugin plugin;

    public BadPacketsCheck(NegativityPlugin plugin) {
        this.plugin = plugin;
    }

    public void onPacket(Player player, PlayerData data, PacketKind kind) {
        // payload spam is evaluated in window
    }

    public void handleWindow(Player player, PlayerData data, Map<PacketKind, Integer> snap) {
        if (!plugin.checks().isEnabled("badpackets")) {
            return;
        }

        int arm = snap.getOrDefault(PacketKind.ARM_ANIMATION, 0);
        int use = snap.getOrDefault(PacketKind.USE_ENTITY, 0);
        int dig = snap.getOrDefault(PacketKind.BLOCK_DIG, 0);
        int place = snap.getOrDefault(PacketKind.BLOCK_PLACE, 0);
        int window = snap.getOrDefault(PacketKind.WINDOW_CLICK, 0);
        int payload = snap.getOrDefault(PacketKind.CUSTOM_PAYLOAD, 0);
        int total = snap.values().stream().mapToInt(Integer::intValue).sum();

        // A: insane total packet rate
        if (total > 120) {
            plugin.checksSupport().flag(player, data, "badpackets", "A",
                    Math.min(6, (total - 120) / 20.0 + 1.5),
                    "total=" + total);
        }

        // B: arm animation spam without combat context
        if (arm > 40 && use == 0) {
            plugin.checksSupport().flag(player, data, "badpackets", "B",
                    Math.min(4, arm / 20.0),
                    "arm=" + arm);
        }

        // C: dig/place spam (nuker-ish at packet level)
        if (dig > 35) {
            plugin.checksSupport().flag(player, data, "badpackets", "C",
                    Math.min(5, dig / 15.0),
                    "dig=" + dig);
        }
        if (place > 30) {
            plugin.checksSupport().flag(player, data, "badpackets", "D",
                    Math.min(5, place / 12.0),
                    "place=" + place);
        }

        // E: inventory click spam
        if (window > 45) {
            plugin.checksSupport().flag(player, data, "badpackets", "E",
                    Math.min(4, window / 20.0),
                    "window=" + window);
        }

        // F: custom payload spam
        if (payload > 40) {
            plugin.checksSupport().flag(player, data, "badpackets", "F",
                    Math.min(5, payload / 15.0),
                    "payload=" + payload);
        }
    }
}
