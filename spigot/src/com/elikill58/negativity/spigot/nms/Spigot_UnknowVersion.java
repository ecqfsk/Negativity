package com.elikill58.negativity.spigot.nms;

import com.elikill58.negativity.spigot.SpigotNegativity;
import com.elikill58.negativity.spigot.utils.PacketUtils;

public class Spigot_UnknowVersion extends SpigotVersionAdapter {
	
	public Spigot_UnknowVersion(String version) {
		super(PacketUtils.getProtocolVersion());
		SpigotNegativity.getInstance().getLogger().warning("Failed to find version adapter for " + version + ".");
	}

	@Override
	public String getTpsFieldName() {
		return "tickTimesNanos";
	}
}
