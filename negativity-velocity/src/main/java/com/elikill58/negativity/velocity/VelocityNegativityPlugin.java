package com.elikill58.negativity.velocity;

import com.elikill58.negativity.common.proxy.ProxyAlertCodec;
import com.elikill58.negativity.proxy.ProxyChannels;
import com.elikill58.negativity.proxy.ProxyMessageValidator;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

@Plugin(
        id = "negativity",
        name = "Negativity",
        version = "2.0.0-SNAPSHOT",
        description = "Negativity Velocity companion",
        authors = {"Elikill58"}
)
public final class VelocityNegativityPlugin {

    private static final MinecraftChannelIdentifier CHANNEL =
            MinecraftChannelIdentifier.from(ProxyChannels.CHANNEL);

    private final ProxyServer server;
    private final Logger logger;

    @Inject
    public VelocityNegativityPlugin(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInit(ProxyInitializeEvent event) {
        server.getChannelRegistrar().register(CHANNEL);
        logger.info("Negativity Velocity companion enabled ({})", ProxyChannels.CHANNEL);
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(CHANNEL)) {
            return;
        }
        boolean fromServer = event.getSource() instanceof ServerConnection;
        boolean fromPlayer = event.getSource() instanceof Player;
        if (!ProxyMessageValidator.isTrustedServerSender(fromServer, fromPlayer)) {
            event.setResult(PluginMessageEvent.ForwardResult.handled());
            logger.warn("Ignored untrusted Negativity plugin message from {}", event.getSource());
            return;
        }
        event.setResult(PluginMessageEvent.ForwardResult.handled());

        try {
            ProxyAlertCodec.AlertPayload alert = ProxyAlertCodec.readAlert(event.getData());
            String serverName = fromServer
                    ? ((ServerConnection) event.getSource()).getServerInfo().getName()
                    : alert.server();

            Component message = Component.text("[Negativity] ", NamedTextColor.RED)
                    .append(Component.text(alert.playerName() + " ", NamedTextColor.YELLOW))
                    .append(Component.text("falhou ", NamedTextColor.GRAY))
                    .append(Component.text(alert.checkId() + " " + alert.subcheck(), NamedTextColor.RED))
                    .append(Component.text(String.format(" | VL: %.1f | Buffer: %.1f | Ping: %dms | TPS: %.1f | @%s",
                            alert.vl(), alert.buffer(), alert.ping(), alert.tps(), serverName), NamedTextColor.GRAY))
                    .hoverEvent(HoverEvent.showText(Component.text(
                            alert.debug() == null || alert.debug().isEmpty() ? "Ir ao servidor" : alert.debug())))
                    .clickEvent(ClickEvent.runCommand("/server " + serverName));

            for (Player staff : server.getAllPlayers()) {
                if (staff.hasPermission("negativity.alert")) {
                    staff.sendMessage(message);
                }
            }
        } catch (Exception e) {
            logger.warn("Invalid Negativity proxy payload: {}", e.getMessage());
        }
    }
}
