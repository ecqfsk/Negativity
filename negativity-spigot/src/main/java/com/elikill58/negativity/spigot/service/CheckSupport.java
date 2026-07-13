package com.elikill58.negativity.spigot.service;

import com.elikill58.negativity.api.event.NegativityFlagEvent;
import com.elikill58.negativity.api.event.NegativityPunishEvent;
import com.elikill58.negativity.api.event.NegativitySetbackEvent;
import com.elikill58.negativity.api.violation.ViolationRecord;
import com.elikill58.negativity.common.check.CheckDefinition;
import com.elikill58.negativity.common.player.PlayerData;
import com.elikill58.negativity.common.violation.ViolationManager;
import com.elikill58.negativity.spigot.NegativityPlugin;
import com.elikill58.negativity.spigot.event.BukkitNegativityFlagEvent;
import com.elikill58.negativity.spigot.event.BukkitNegativityPunishEvent;
import com.elikill58.negativity.spigot.event.BukkitNegativitySetbackEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Shared flag pipeline used by every check.
 */
public final class CheckSupport {

    private final NegativityPlugin plugin;

    public CheckSupport(@NotNull NegativityPlugin plugin) {
        this.plugin = plugin;
    }

    public void flag(
            @NotNull Player player,
            @NotNull PlayerData data,
            @NotNull String checkId,
            @NotNull String subcheck,
            double amount,
            @Nullable String debug
    ) {
        if (amount <= 0 || !data.isAnalyzed()) {
            return;
        }
        if (plugin.exemptions().isExempt(player.getUniqueId(), checkId)) {
            return;
        }
        CheckDefinition def = plugin.checks().getDefinition(checkId).orElse(null);
        if (def == null || !def.enabled()) {
            return;
        }

        ViolationManager.FlagResult result = plugin.violations().flag(new ViolationManager.FlagRequest(
                player.getUniqueId(),
                player.getName(),
                def.id(),
                subcheck,
                amount,
                data.ping(),
                plugin.metrics().tps(),
                plugin.config().serverName(),
                debug,
                def.thresholds()
        ));

        ViolationRecord record = new ViolationRecord(
                player.getUniqueId(),
                player.getName(),
                def.id(),
                subcheck,
                result.vl(),
                result.buffer(),
                data.ping(),
                plugin.metrics().tps(),
                plugin.config().serverName(),
                System.currentTimeMillis(),
                debug
        );

        NegativityFlagEvent apiEvent = new NegativityFlagEvent(record);
        BukkitNegativityFlagEvent bukkitEvent = new BukkitNegativityFlagEvent(apiEvent);
        Bukkit.getPluginManager().callEvent(bukkitEvent);
        if (apiEvent.isCancelled() || bukkitEvent.isCancelled()) {
            return;
        }

        plugin.storage().insertAsync(record);

        if (result.shouldAlert()) {
            plugin.alerts().handleFlag(record, result, def.displayName());
            plugin.proxy().sendAlert(player, record);
        }

        if (result.shouldSetback() && def.thresholds().setback()) {
            NegativitySetbackEvent setbackApi = new NegativitySetbackEvent(player.getUniqueId(), def.id());
            BukkitNegativitySetbackEvent setbackBukkit = new BukkitNegativitySetbackEvent(setbackApi);
            Bukkit.getPluginManager().callEvent(setbackBukkit);
            if (!setbackApi.isCancelled() && !setbackBukkit.isCancelled()) {
                plugin.setbacks().setback(player, data);
            }
        }

        if (result.shouldPunish() && plugin.config().punishmentsEnabled()) {
            List<String> commands = plugin.config().checkCommands(def.id());
            NegativityPunishEvent punishApi = new NegativityPunishEvent(record, commands);
            BukkitNegativityPunishEvent punishBukkit = new BukkitNegativityPunishEvent(punishApi);
            Bukkit.getPluginManager().callEvent(punishBukkit);
            if (punishApi.isCancelled() || punishBukkit.isCancelled()) {
                return;
            }
            for (String cmd : punishApi.commands()) {
                String parsed = cmd
                        .replace("%player%", player.getName())
                        .replace("%uuid%", player.getUniqueId().toString())
                        .replace("%check%", def.displayName())
                        .replace("%subcheck%", subcheck)
                        .replace("%vl%", String.format("%.1f", result.vl()))
                        .replace("%buffer%", String.format("%.1f", result.buffer()))
                        .replace("%ping%", String.valueOf(data.ping()))
                        .replace("%tps%", String.format("%.1f", plugin.metrics().tps()))
                        .replace("%server%", plugin.config().serverName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
            }
        }
    }
}
