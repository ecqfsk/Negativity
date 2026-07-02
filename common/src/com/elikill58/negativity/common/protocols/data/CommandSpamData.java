package com.elikill58.negativity.common.protocols.data;

import com.elikill58.negativity.api.NegativityPlayer;
import com.elikill58.negativity.api.protocols.CheckData;

public class CommandSpamData extends CheckData {

	public long windowStart = 0;
	public int count = 0;

	public CommandSpamData(NegativityPlayer np) {
		super(np);
	}
}
