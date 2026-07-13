package com.elikill58.negativity.spigot.service;

import com.elikill58.negativity.api.violation.ViolationRecord;
import com.elikill58.negativity.common.proxy.ProxyAlertCodec;
import com.elikill58.negativity.spigot.NegativityPlugin;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

/**
 * Sends trusted alert payloads to Bungee/Velocity companions.
 */
public final class ProxyBridge implements PluginMessageListener {

    private final NegativityPlugin plugin;

    public ProxyBridge(@NotNull NegativityPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        if (!plugin.config().proxyEnabled()) {
            return;
        }
        String channel = plugin.config().proxyChannel();
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, channel);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, channel, this);
        plugin.getLogger().info("Proxy channel registered: " + channel);
    }

    public void unregister() {
        try {
            String channel = plugin.config().proxyChannel();
            plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin, channel);
            plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin, channel, this);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Proxy channel unregister failed", e);
        }
    }

    public void sendAlert(@NotNull Player player, @NotNull ViolationRecord record) {
        if (!plugin.config().proxyEnabled()) {
            return;
        }
        try {
            byte[] payload = ProxyAlertCodec.writeAlert(new ProxyAlertCodec.AlertPayload(
                    record.playerId(),
                    record.playerName(),
                    record.checkId(),
                    record.subcheck(),
                    record.vl(),
                    record.buffer(),
                    record.ping(),
                    record.tps(),
                    record.server() == null ? plugin.config().serverName() : record.server(),
                    record.debug() == null ? "" : record.debug()
            ));
            player.sendPluginMessage(plugin, plugin.config().proxyChannel(), payload);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to send proxy alert", e);
        }
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NotNull [] message) {
        // Backend should not accept untrusted player→server negativity payloads for actions
        if (plugin.config().proxyRequireServerSender()) {
            // ignore inbound from clients on backend; companion talks server-side on proxy
            return;
        }
    }
}
