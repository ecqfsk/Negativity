package com.elikill58.negativity.common.protocols.data;

import com.elikill58.negativity.api.NegativityPlayer;
import com.elikill58.negativity.api.protocols.CheckData;

public class IllegalItemsData extends CheckData {

	public long lastScan = 0;

	public IllegalItemsData(NegativityPlayer np) {
		super(np);
	}
}
