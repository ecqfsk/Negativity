package com.elikill58.negativity.api.packets.packet.playout;

import com.elikill58.negativity.api.location.Vector;
import com.elikill58.negativity.api.packets.PacketType;
import com.elikill58.negativity.api.packets.nms.PacketSerializer;
import com.elikill58.negativity.api.packets.packet.NPacketPlayOut;
import com.elikill58.negativity.universal.Version;

public class NPacketPlayOutEntityVelocity implements NPacketPlayOut {

	public int entityId;
	public Vector vec;

	public NPacketPlayOutEntityVelocity() {
		
	}
	
	public NPacketPlayOutEntityVelocity(int entityId, int x, int y, int z) {
		this.entityId = entityId;
		this.vec = new Vector(x, y, z);
	}
	
	@Override
	public void read(PacketSerializer serializer, Version version) {
		this.entityId = serializer.readVarInt();
		if (version.isNewerOrEquals(Version.V1_21_10)) {
			// 1.21.9+ packs the velocity as a low-precision Vec3 (blocks/tick) instead of
			// 3 shorts: scale to 1/8000 blocks/tick so consumers keep the historical unit
			this.vec = serializer.readLpVec3().multiply(new Vector(8000, 8000, 8000));
		} else {
			this.vec = serializer.readShortVector();
		}
	}

	@Override
	public PacketType getPacketType() {
		return PacketType.Server.ENTITY_VELOCITY;
	}
}
