package com.elikill58.negativity.bungee;

import com.elikill58.negativity.common.proxy.ProxyAlertCodec;
import com.elikill58.negativity.proxy.ProxyChannels;
import com.elikill58.negativity.proxy.ProxyMessageValidator;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

/**
 * BungeeCord companion — fans out trusted backend alerts to staff.
 */
public final class BungeeNegativityPlugin extends Plugin implements Listener {

    @Override
    public void onEnable() {
        getProxy().registerChannel(ProxyChannels.CHANNEL);
        getProxy().getPluginManager().registerListener(this, this);
        getLogger().info("Negativity Bungee companion enabled (" + ProxyChannels.CHANNEL + ")");
    }

    @Override
    public void onDisable() {
        getProxy().unregisterChannel(ProxyChannels.CHANNEL);
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (!ProxyMessageValidator.isPlausibleChannel(event.getTag())) {
            return;
        }
        boolean fromServer = event.getSender() instanceof Server;
        boolean fromPlayer = event.getSender() instanceof ProxiedPlayer;
        if (!ProxyMessageValidator.isTrustedServerSender(fromServer, fromPlayer)) {
            event.setCancelled(true);
            getLogger().warning("Ignored untrusted Negativity plugin message from " + event.getSender());
            return;
        }
        event.setCancelled(true);

        try {
            ProxyAlertCodec.AlertPayload alert = ProxyAlertCodec.readAlert(event.getData());
            String serverName = fromServer ? ((Server) event.getSender()).getInfo().getName() : alert.server();
            String line = ChatColor.RED + "[Negativity] " + ChatColor.YELLOW + alert.playerName()
                    + ChatColor.GRAY + " falhou " + ChatColor.RED + alert.checkId() + " " + alert.subcheck()
                    + ChatColor.GRAY + " | VL: " + ChatColor.WHITE + String.format("%.1f", alert.vl())
                    + ChatColor.GRAY + " | Buffer: " + ChatColor.WHITE + String.format("%.1f", alert.buffer())
                    + ChatColor.GRAY + " | Ping: " + ChatColor.WHITE + alert.ping() + "ms"
                    + ChatColor.GRAY + " | TPS: " + ChatColor.WHITE + String.format("%.1f", alert.tps())
                    + ChatColor.GRAY + " | @" + serverName;

            for (ProxiedPlayer staff : getProxy().getPlayers()) {
                if (!staff.hasPermission("negativity.alert")) {
                    continue;
                }
                TextComponent msg = new TextComponent(line);
                msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder(alert.debug() == null || alert.debug().isEmpty()
                                ? "Clique para ir"
                                : alert.debug()).create()));
                msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/server " + serverName));
                staff.sendMessage(msg);
            }
        } catch (Exception e) {
            getLogger().warning("Invalid Negativity proxy payload: " + e.getMessage());
        }
    }
}
