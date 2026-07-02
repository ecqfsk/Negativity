package com.elikill58.negativity.common.protocols.data;

import com.elikill58.negativity.api.NegativityPlayer;
import com.elikill58.negativity.api.location.Location;
import com.elikill58.negativity.api.protocols.CheckData;

public class XRayData extends CheckData {

	/** Last broken block of the current mining session. */
	public Location lastBreakLoc;
	/** Normalized direction between the two previously broken blocks. */
	public double dirX = 0, dirY = 0, dirZ = 0;
	public boolean hasDir = false;
	/** Direction changes while mining, and how many pointed at a hidden rare ore. */
	public int totalTurns = 0, luckyTurns = 0;
	/** Timestamp of the last mining-related block break. */
	public long mining = 0;

	public XRayData(NegativityPlayer np) {
		super(np);
	}
}
