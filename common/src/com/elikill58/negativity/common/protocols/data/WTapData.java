package com.elikill58.negativity.common.protocols.data;

import com.elikill58.negativity.api.NegativityPlayer;
import com.elikill58.negativity.api.protocols.CheckData;

public class WTapData extends CheckData {

	public boolean lastSprint = false;
	public int toggles = 0;
	public int moveCounter = 0;

	public WTapData(NegativityPlayer np) {
		super(np);
	}
}
