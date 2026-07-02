package com.elikill58.negativity.common.protocols;

import static com.elikill58.negativity.universal.detections.keys.CheatKeys.WTAP;

import com.elikill58.negativity.api.NegativityPlayer;
import com.elikill58.negativity.api.entity.Player;
import com.elikill58.negativity.api.events.player.PlayerMoveEvent;
import com.elikill58.negativity.api.item.Materials;
import com.elikill58.negativity.api.protocols.Check;
import com.elikill58.negativity.api.protocols.CheckConditions;
import com.elikill58.negativity.common.protocols.data.WTapData;
import com.elikill58.negativity.universal.Negativity;
import com.elikill58.negativity.universal.detections.Cheat;
import com.elikill58.negativity.universal.report.ReportType;
import com.elikill58.negativity.universal.utils.UniversalUtils;

/**
 * WTap detector.
 *
 * <p>W-tapping toggles sprint (release + re-press W) to reset the sprint knockback bonus.
 * Legit players manage ~3-5 toggles per second at best; a cheat toggles far faster. Only
 * counted while in fight: outside of combat there is no reason to w-tap, and vanilla
 * sprint resets (e.g. on hit) otherwise pollute the counter. The window is time-based
 * (1 second) so the toggle rate is compared against a real per-second bound.
 */
public class WTap extends Cheat {

	private static final long WINDOW_MS = 1000L;

	public WTap() {
		super(WTAP, CheatCategory.COMBAT, Materials.BLAZE_ROD, WTapData::new);
	}

	@Check(name = "sprint-toggle", description = "Inhuman sprint toggling (w-tap)", conditions = { CheckConditions.SURVIVAL })
	public void onPlayerMove(PlayerMoveEvent e, NegativityPlayer np, WTapData data) {
		Player p = e.getPlayer();
		if (!np.isInFight)
			return;
		boolean sprint = p.isSprinting();
		if (sprint == data.lastSprint)
			return;
		data.lastSprint = sprint;

		long now = System.currentTimeMillis();
		if (now - data.windowStart > WINDOW_MS) {
			data.windowStart = now;
			data.toggles = 0;
		}
		data.toggles++;
		int alertAt = getConfig().getInt("toggle_alert", 8);
		if (data.toggles > alertAt) {
			int reliability = UniversalUtils.parseInPorcent(60 + data.toggles * 3);
			Negativity.alertMod(ReportType.WARNING, p, this, reliability, "sprint-toggle",
					"Sprint toggled " + data.toggles + " times within 1s while in fight");
			data.toggles = alertAt; // partial reset to avoid spamming every move
		}
	}
}
