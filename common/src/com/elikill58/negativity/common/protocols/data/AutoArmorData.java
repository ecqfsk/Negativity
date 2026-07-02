package com.elikill58.negativity.common.protocols.data;

import com.elikill58.negativity.api.NegativityPlayer;
import com.elikill58.negativity.api.protocols.CheckData;

public class AutoArmorData extends CheckData {

	public long lastEquipTime = 0;
	public int equipStreak = 0;

	public AutoArmorData(NegativityPlayer np) {
		super(np);
	}
}
