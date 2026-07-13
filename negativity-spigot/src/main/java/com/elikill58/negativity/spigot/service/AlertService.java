package com.elikill58.negativity.spigot.service;

import com.elikill58.negativity.api.violation.ViolationRecord;
import com.elikill58.negativity.common.violation.ViolationManager;
import com.elikill58.negativity.spigot.NegativityPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AlertService {

    private final NegativityPlugin plugin;
    private final SpigotConfig config;
    private final Map<UUID, Boolean> alertsToggled = new ConcurrentHashMap<>();

    public AlertService(NegativityPlugin plugin, SpigotConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void setAlerts(Player staff, boolean enabled) {
        alertsToggled.put(staff.getUniqueId(), enabled);
    }

    public boolean hasAlerts(Player staff) {
        return alertsToggled.getOrDefault(staff.getUniqueId(), true);
    }

    public void handleFlag(ViolationRecord record, ViolationManager.FlagResult result, String checkDisplay) {
        if (!config.alertsEnabled() || !result.shouldAlert()) {
            return;
        }

        String format = config.alertFormat()
                .replace("%player%", record.playerName())
                .replace("%check%", checkDisplay)
                .replace("%subcheck%", record.subcheck())
                .replace("%vl%", String.format(Locale.ROOT, "%.1f", record.vl()))
                .replace("%buffer%", String.format(Locale.ROOT, "%.1f", record.buffer()))
                .replace("%ping%", String.valueOf(record.ping()))
                .replace("%tps%", String.format(Locale.ROOT, "%.1f", record.tps()))
                .replace("%server%", record.server() == null ? config.serverName() : record.server());

        Component message = LegacyComponentSerializer.legacyAmpersand().deserialize(format);
        String hover = "Check: " + record.checkId() + "/" + record.subcheck()
                + "\nVL: " + String.format(Locale.ROOT, "%.2f", record.vl())
                + "\nBuffer: " + String.format(Locale.ROOT, "%.2f", record.buffer())
                + "\nPing: " + record.ping() + "ms"
                + "\nTPS: " + String.format(Locale.ROOT, "%.2f", record.tps())
                + (record.debug() == null ? "" : "\n" + record.debug());
        message = message
                .hoverEvent(HoverEvent.showText(Component.text(hover)))
                .clickEvent(ClickEvent.runCommand("/negativity check " + record.playerName()));

        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (!staff.hasPermission(config.alertPermission())) {
                continue;
            }
            if (!hasAlerts(staff)) {
                continue;
            }
            staff.sendMessage(message);
        }

        if (config.debug()) {
            plugin.getLogger().info("[ALERT] " + LegacyComponentSerializer.legacySection().serialize(message));
        }
    }

    public void clear(Player player) {
        alertsToggled.remove(player.getUniqueId());
    }
}
