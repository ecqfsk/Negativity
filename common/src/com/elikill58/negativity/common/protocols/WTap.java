package com.elikill58.negativity.common.protocols;

import static com.elikill58.negativity.universal.detections.keys.CheatKeys.WTAP;

import com.elikill58.negativity.api.NegativityPlayer;
import com.elikill58.negativity.api.entity.Player;
import com.elikill58.negativity.api.events.player.PlayerMoveEvent;
import com.elikill58.negativity.api.item.Materials;
import com.elikill58.negativity.api.protocols.Check;
import com.elikill58.negativity.common.protocols.data.WTapData;
import com.elikill58.negativity.universal.Negativity;
import com.elikill58.negativity.universal.detections.Cheat;
import com.elikill58.negativity.universal.report.ReportType;
import com.elikill58.negativity.universal.utils.UniversalUtils;

/**
 * WTap detector.
 *
 * <p>W-tapping rapidly toggles the sprint state (release + re-press W) to reset the
 * sprint-reset knockback bonus on almost every hit. A human tapping is bounded; a
 * client doing it programmatically toggles far more often. We count sprint toggles over
 * a sliding window of moves and alert when the toggle rate is inhuman.
 */
public class WTap extends Cheat {

	// Number of move events forming the sliding window
	private static final int WINDOW_MOVES = 40;

	public WTap() {
		super(WTAP, CheatCategory.COMBAT, Materials.BLAZE_ROD, WTapData::new);
	}

	@Check(name = "sprint-toggle", description = "Inhuman sprint toggling (w-tap)")
	public void onPlayerMove(PlayerMoveEvent e, NegativityPlayer np, WTapData data) {
		Player p = e.getPlayer();
		boolean sprint = p.isSprinting();
		if (sprint != data.lastSprint) {
			data.toggles++;
			data.lastSprint = sprint;
		}
		data.moveCounter++;
		if (data.moveCounter >= WINDOW_MOVES) {
			int alertAt = getConfig().getInt("toggle_alert", 14);
			if (data.toggles >= alertAt) {
				int reliability = UniversalUtils.parseInPorcent(55 + (data.toggles - alertAt) * 4);
				Negativity.alertMod(ReportType.WARNING, p, this, reliability, "sprint-toggle",
						"Sprint toggled " + data.toggles + " times over " + WINDOW_MOVES + " moves");
			}
			data.moveCounter = 0;
			data.toggles = 0;
		}
	}
}
