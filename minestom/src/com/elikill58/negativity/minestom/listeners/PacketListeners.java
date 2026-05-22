package com.elikill58.negativity.minestom.listeners;

import com.elikill58.negativity.api.entity.Player;
import com.elikill58.negativity.api.events.EventManager;
import com.elikill58.negativity.api.events.packets.PacketPreReceiveEvent;
import com.elikill58.negativity.api.events.packets.PacketReceiveEvent;
import com.elikill58.negativity.api.events.packets.PacketSendEvent;
import com.elikill58.negativity.api.packets.packet.NPacket;
import com.elikill58.negativity.minestom.impl.entity.MinestomEntityManager;
import com.elikill58.negativity.minestom.nms.MinestomPlayPackets;

import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.event.player.PlayerPacketOutEvent;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.server.ServerPacket;

public class PacketListeners {

	public PacketListeners(EventNode<Event> e) {
		e.addListener(PlayerPacketEvent.class, this::onPacket);
		e.addListener(PlayerPacketOutEvent.class, this::onPacket);
	}

	public void onPacket(PlayerPacketEvent e) {
		ClientPacket cp = e.getPacket();
		Player p = MinestomEntityManager.getPlayer(e.getEntity());
		NPacket packet = MinestomPlayPackets.Client.build(cp);
		if (packet == null || packet.getPacketType().isUnset()) {
			return;
		}
		PacketPreReceiveEvent event = new PacketPreReceiveEvent(packet, p);
		EventManager.callEvent(event);
		if (event.isCancelled())
			e.setCancelled(true);
		EventManager.callEvent(new PacketReceiveEvent(packet, p));
	}

	public void onPacket(PlayerPacketOutEvent e) {
		ServerPacket sp = e.getPacket();
		Player p = MinestomEntityManager.getPlayer(e.getEntity());
		NPacket packet = MinestomPlayPackets.Server.build(sp);
		if (packet == null || packet.getPacketType().isUnset()) {
			return;
		}
		EventManager.callEvent(new PacketSendEvent(packet, p));
	}
}
