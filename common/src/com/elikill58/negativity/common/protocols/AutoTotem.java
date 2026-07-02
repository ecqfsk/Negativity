package com.elikill58.negativity.common.protocols;

import static com.elikill58.negativity.universal.detections.keys.CheatKeys.AUTO_TOTEM;

import java.util.Locale;

import com.elikill58.negativity.api.NegativityPlayer;
import com.elikill58.negativity.api.entity.Player;
import com.elikill58.negativity.api.events.entity.EntityResurrectionEvent;
import com.elikill58.negativity.api.item.ItemStack;
import com.elikill58.negativity.api.item.Materials;
import com.elikill58.negativity.api.protocols.Check;
import com.elikill58.negativity.common.protocols.data.EmptyData;
import com.elikill58.negativity.universal.Negativity;
import com.elikill58.negativity.universal.Scheduler;
import com.elikill58.negativity.universal.detections.Cheat;
import com.elikill58.negativity.universal.report.ReportType;
import com.elikill58.negativity.universal.utils.UniversalUtils;

/**
 * AutoTotem detector.
 *
 * <p>When a totem of undying saves a player, its slot is emptied that tick. A human needs
 * reaction time to move a new totem back into the off-hand; an AutoTotem client refills it
 * within a tick. We snapshot the off-hand at resurrection (should be empty, the totem was
 * just consumed) and re-check it a few ticks later: an empty-&gt;totem transition faster than
 * the configured threshold is inhuman. Comparing before/after avoids flagging players who
 * simply hold a second totem in the main hand.
 */
public class AutoTotem extends Cheat {

	public AutoTotem() {
		super(AUTO_TOTEM, CheatCategory.COMBAT, Materials.GOLDEN_APPLE, EmptyData::new);
	}

	@Check(name = "instant-refill", description = "Off-hand totem refilled faster than humanly possible")
	public void onResurrect(EntityResurrectionEvent e, NegativityPlayer np) {
		Player p = e.getPlayer();
		boolean hadTotemBefore = isTotem(p.getItemInOffHand());
		long resurrectedAt = System.currentTimeMillis();
		int checkTicks = getConfig().getInt("check_ticks", 1);
		long suspiciousMs = getConfig().getInt("suspicious_ms", 120);

		Scheduler.getInstance().runEntityDelayed(p, () -> {
			if (!p.isOnline())
				return;
			if (!hadTotemBefore && isTotem(p.getItemInOffHand())) {
				long elapsed = System.currentTimeMillis() - resurrectedAt;
				if (elapsed <= suspiciousMs) {
					int reliability = UniversalUtils.parseInPorcent(85 + (suspiciousMs - elapsed) / 4);
					Negativity.alertMod(ReportType.WARNING, p, this, reliability, "instant-refill",
							"Off-hand totem refilled " + elapsed + "ms after resurrection (max " + suspiciousMs + "ms)");
				}
			}
		}, checkTicks);
	}

	private boolean isTotem(ItemStack item) {
		return item != null && item.getType().getId().toLowerCase(Locale.ROOT).contains("totem");
	}
}
