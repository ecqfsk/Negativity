package com.elikill58.negativity.spigot.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.elikill58.negativity.common.packet.PacketKind;
import com.elikill58.negativity.spigot.NegativityPlugin;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * ProtocolLib integration. Loaded only when ProtocolLib is present.
 */
public final class ProtocolLibBridge {

    private ProtocolLibBridge() {
    }

    public static void install(NegativityPlugin plugin, PacketService service) {
        List<PacketType> types = new ArrayList<>();
        for (PacketType type : PacketType.Play.Client.getInstance()) {
            if (type.isSupported()) {
                types.add(type);
            }
        }
        // Fallback known set if enumeration fails empty
        if (types.isEmpty()) {
            addIfPresent(types, "FLYING");
            addIfPresent(types, "GROUND");
            addIfPresent(types, "POSITION");
            addIfPresent(types, "LOOK");
            addIfPresent(types, "POSITION_LOOK");
            addIfPresent(types, "ARM_ANIMATION");
            addIfPresent(types, "USE_ENTITY");
            addIfPresent(types, "ENTITY_ACTION");
            addIfPresent(types, "BLOCK_DIG");
            addIfPresent(types, "BLOCK_PLACE");
            addIfPresent(types, "USE_ITEM");
            addIfPresent(types, "WINDOW_CLICK");
            addIfPresent(types, "ABILITIES");
            addIfPresent(types, "CUSTOM_PAYLOAD");
            addIfPresent(types, "HELD_ITEM_SLOT");
        }

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(
                plugin,
                ListenerPriority.LOWEST,
                types
        ) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.isPlayerTemporary()) {
                    return;
                }
                Player player = event.getPlayer();
                if (player == null) {
                    return;
                }
                PacketKind kind = PacketService.mapProtocolLibName(event.getPacketType().name());
                service.onPacket(player, kind);
            }
        });
    }

    private static void addIfPresent(List<PacketType> types, String field) {
        try {
            PacketType type = (PacketType) PacketType.Play.Client.class.getField(field).get(null);
            if (type != null && type.isSupported()) {
                types.add(type);
            }
        } catch (Throwable ignored) {
        }
    }
}
