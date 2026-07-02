package com.elikill58.negativity.common.protocols;

import static com.elikill58.negativity.universal.detections.keys.CheatKeys.BOAT_FLY;

import com.elikill58.negativity.api.NegativityPlayer;
import com.elikill58.negativity.api.entity.Player;
import com.elikill58.negativity.api.events.player.PlayerMoveEvent;
import com.elikill58.negativity.api.item.Materials;
import com.elikill58.negativity.api.location.Location;
import com.elikill58.negativity.api.protocols.Check;
import com.elikill58.negativity.universal.Negativity;
import com.elikill58.negativity.universal.detections.Cheat;
import com.elikill58.negativity.universal.report.ReportType;
import com.elikill58.negativity.universal.utils.UniversalUtils;

/**
 * BoatFly detector.
 *
 * <p>A boat has no engine: on flat ground it can only descend or stay level. A client
 * gaining altitude while riding a boat (boat-fly / boat-glide) rises tick after tick.
 * We buffer consecutive upward ticks and alert once the streak is physically impossible.
 */
public class BoatFly extends Cheat {

	// Upward dy per tick considered a real climb (filters micro bobbing on water)
	private static final double RISE_THRESHOLD = 0.1;

	public BoatFly() {
		super(BOAT_FLY, CheatCategory.MOVEMENT, Materials.CHEST, com.elikill58.negativity.common.protocols.data.BoatFlyData::new);
	}

	@Check(name = "boat-ascend", description = "Gaining altitude while riding a boat")
	public void onPlayerMove(PlayerMoveEvent e, NegativityPlayer np, com.elikill58.negativity.common.protocols.data.BoatFlyData data) {
		Player p = e.getPlayer();
		if (!p.isInBoat())
			return;
		Location from = e.getFrom(), to = e.getTo();
		double dy = to.getY() - from.getY();
		if (dy > RISE_THRESHOLD) {
			data.ascendBuffer++;
			int alertAt = getConfig().getInt("ascend_alert", 5);
			if (data.ascendBuffer >= alertAt) {
				int reliability = UniversalUtils.parseInPorcent(65 + (data.ascendBuffer - alertAt) * 6);
				boolean mayCancel = Negativity.alertMod(ReportType.WARNING, p, this, reliability, "boat-ascend",
						"Boat gaining altitude for " + data.ascendBuffer + " ticks (last dy: " + dy + ")") && isSetBack();
				if (mayCancel)
					e.setCancelled(true);
			}
		} else if (data.ascendBuffer > 0) {
			data.ascendBuffer--;
		}
	}
}
