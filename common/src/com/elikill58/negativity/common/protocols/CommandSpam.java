package com.elikill58.negativity.common.protocols;

import static com.elikill58.negativity.universal.detections.keys.CheatKeys.COMMAND_SPAM;

import com.elikill58.negativity.api.NegativityPlayer;
import com.elikill58.negativity.api.entity.Player;
import com.elikill58.negativity.api.events.player.PlayerCommandPreProcessEvent;
import com.elikill58.negativity.api.item.Materials;
import com.elikill58.negativity.api.protocols.Check;
import com.elikill58.negativity.common.protocols.data.CommandSpamData;
import com.elikill58.negativity.universal.Negativity;
import com.elikill58.negativity.universal.detections.Cheat;
import com.elikill58.negativity.universal.report.ReportType;
import com.elikill58.negativity.universal.utils.UniversalUtils;

/**
 * CommandSpam detector.
 *
 * <p>Counts commands sent within a rolling one-second window. Beyond a configurable
 * threshold the traffic is a spam/crash attempt rather than manual typing.
 */
public class CommandSpam extends Cheat {

	private static final long WINDOW_MS = 1000L;

	public CommandSpam() {
		super(COMMAND_SPAM, CheatCategory.PLAYER, Materials.PAPER, CommandSpamData::new);
	}

	@Check(name = "rate", description = "Too many commands per second")
	public void onCommand(PlayerCommandPreProcessEvent e, NegativityPlayer np, CommandSpamData data) {
		Player p = e.getPlayer();
		long now = System.currentTimeMillis();
		if (now - data.windowStart > WINDOW_MS) {
			data.windowStart = now;
			data.count = 0;
		}
		data.count++;
		int alertAt = getConfig().getInt("commands_per_second_alert", 6);
		if (data.count > alertAt) {
			int reliability = UniversalUtils.parseInPorcent(55 + (data.count - alertAt) * 8);
			boolean cancel = Negativity.alertMod(ReportType.WARNING, p, this, reliability, "rate",
					data.count + " commands within 1s (limit " + alertAt + "): " + e.getCommand()) && isSetBack();
			if (cancel)
				e.setCancelled(true);
		}
	}
}
