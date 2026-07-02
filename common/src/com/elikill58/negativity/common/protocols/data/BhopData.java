package com.elikill58.negativity.common.protocols.data;

import com.elikill58.negativity.api.NegativityPlayer;
import com.elikill58.negativity.api.protocols.CheckData;

public class BhopData extends CheckData {

	public boolean wasOnGround = true;
	public int buffer = 0;

	public BhopData(NegativityPlayer np) {
		super(np);
	}
}
