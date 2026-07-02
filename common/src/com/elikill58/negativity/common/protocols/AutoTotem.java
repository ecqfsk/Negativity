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
 * <p>Timing matters: when the resurrection event fires, the consumed totem is STILL in the
 * player's hand — it is removed right after the event. Vanilla consumes the main hand first
 * if it holds a totem, otherwise the off-hand. So we note which hand is about to be emptied
 * and re-check that same hand shortly after: if it holds a totem again within check_ticks
 * ticks (~50ms each), no human moved it there. Checking the consumed hand only avoids
 * flagging players who legitimately hold a spare totem in the other hand, and stacked
 * (non-vanilla) totems are skipped since the hand would not empty at all.
 */
public class AutoTotem extends Cheat {

	public AutoTotem() {
		super(AUTO_TOTEM, CheatCategory.COMBAT, Materials.GOLDEN_APPLE, EmptyData::new);
	}

	@Check(name = "instant-refill", description = "Consumed-hand totem refilled faster than humanly possible")
	public void onResurrect(EntityResurrectionEvent e, NegativityPlayer np) {
		Player p = e.getPlayer();
		boolean mainHad = isTotem(p.getItemInHand());
		boolean offHad = isTotem(p.getItemInOffHand());
		if (!mainHad && !offHad)
			return; // no visible totem (plugin-driven resurrection): no baseline to compare
		boolean consumedMain = mainHad; // vanilla consumes the main hand first
		ItemStack consumed = consumedMain ? p.getItemInHand() : p.getItemInOffHand();
		if (consumed.getAmount() > 1)
			return; // non-vanilla stacked totems: the hand will not empty, undetectable

		int checkTicks = getConfig().getInt("check_ticks", 1);
		Scheduler.getInstance().runEntityDelayed(p, () -> {
			if (!p.isOnline())
				return;
			ItemStack handNow = consumedMain ? p.getItemInHand() : p.getItemInOffHand();
			if (isTotem(handNow)) {
				int reliability = UniversalUtils.parseInPorcent(90 + (checkTicks <= 1 ? 8 : 0));
				Negativity.alertMod(ReportType.WARNING, p, this, reliability, "instant-refill",
						"Totem back in " + (consumedMain ? "main hand" : "off hand") + " " + checkTicks
								+ " tick(s) after resurrection");
			}
		}, checkTicks);
	}

	private boolean isTotem(ItemStack item) {
		return item != null && item.getType().getId().toLowerCase(Locale.ROOT).contains("totem");
	}
}
