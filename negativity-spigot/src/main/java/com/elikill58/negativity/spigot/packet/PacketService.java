package com.elikill58.negativity.spigot.packet;

import com.elikill58.negativity.common.packet.PacketKind;
import com.elikill58.negativity.common.player.PlayerData;
import com.elikill58.negativity.spigot.NegativityPlugin;
import com.elikill58.negativity.spigot.check.BadPacketsCheck;
import com.elikill58.negativity.spigot.check.TimerCheck;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.logging.Level;

/**
 * Central packet ingestion. ProtocolLib when present; Bukkit approximations otherwise.
 */
public final class PacketService {

    private final NegativityPlugin plugin;
    private final TimerCheck timerCheck;
    private final BadPacketsCheck badPacketsCheck;
    private @Nullable BukkitTask analyzeTask;
    private boolean protocolLib;

    public PacketService(@NotNull NegativityPlugin plugin) {
        this.plugin = plugin;
        this.timerCheck = new TimerCheck(plugin);
        this.badPacketsCheck = new BadPacketsCheck(plugin);
    }

    public void start() {
        protocolLib = tryHookProtocolLib();
        if (protocolLib) {
            plugin.getLogger().info("Packet layer: ProtocolLib");
        } else {
            plugin.getLogger().info("Packet layer: Bukkit approximation (install ProtocolLib for full accuracy)");
        }
        analyzeTask = Bukkit.getScheduler().runTaskTimer(plugin, this::analyzeWindows, 20L, 20L);
    }

    public void stop() {
        if (analyzeTask != null) {
            analyzeTask.cancel();
            analyzeTask = null;
        }
    }

    public void onPacket(@NotNull Player player, @NotNull PacketKind kind) {
        PlayerData data = plugin.players().getOrCreate(player.getUniqueId(), player.getName());
        data.packets().increment(kind);
        data.latency().recordPacket();
        badPacketsCheck.onPacket(player, data, kind);
    }

    private void analyzeWindows() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            plugin.players().getData(player.getUniqueId()).ifPresent(data -> {
                Map<PacketKind, Integer> snap = data.packets().snapshotAndReset();
                timerCheck.handle(player, data, snap);
                badPacketsCheck.handleWindow(player, data, snap);
            });
        }
    }

    private boolean tryHookProtocolLib() {
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") == null) {
            return false;
        }
        try {
            ProtocolLibBridge.install(plugin, this);
            return true;
        } catch (Throwable t) {
            plugin.getLogger().log(Level.WARNING, "ProtocolLib hook failed, using Bukkit approximation", t);
            return false;
        }
    }

    public boolean usesProtocolLib() {
        return protocolLib;
    }

    public static PacketKind mapProtocolLibName(String name) {
        if (name == null) {
            return PacketKind.OTHER;
        }
        String n = name.toUpperCase();
        if (n.contains("POSITION_LOOK") || n.equals("POSITIONLOOK")) return PacketKind.POSITION_LOOK;
        if (n.contains("POSITION")) return PacketKind.POSITION;
        if (n.contains("LOOK") || n.contains("ROTATION")) return PacketKind.LOOK;
        if (n.contains("FLYING") || n.contains("GROUND")) return PacketKind.FLYING;
        if (n.contains("ARM_ANIMATION") || n.contains("SWING")) return PacketKind.ARM_ANIMATION;
        if (n.contains("USE_ENTITY")) return PacketKind.USE_ENTITY;
        if (n.contains("ENTITY_ACTION")) return PacketKind.ENTITY_ACTION;
        if (n.contains("BLOCK_DIG") || n.contains("DIGGING")) return PacketKind.BLOCK_DIG;
        if (n.contains("BLOCK_PLACE") || n.contains("USE_ITEM") || n.contains("PLACE")) return PacketKind.BLOCK_PLACE;
        if (n.contains("WINDOW_CLICK")) return PacketKind.WINDOW_CLICK;
        if (n.contains("ABILITIES")) return PacketKind.ABILITIES;
        if (n.contains("CUSTOM_PAYLOAD") || n.contains("PLUGIN_MESSAGE")) return PacketKind.CUSTOM_PAYLOAD;
        if (n.contains("HELD")) return PacketKind.HELD_ITEM;
        if (n.contains("STEER")) return PacketKind.STEER_VEHICLE;
        if (n.contains("TRANSACTION") || n.contains("PONG")) return PacketKind.TRANSACTION;
        if (n.contains("KEEP_ALIVE")) return PacketKind.KEEP_ALIVE;
        return PacketKind.OTHER;
    }
}
