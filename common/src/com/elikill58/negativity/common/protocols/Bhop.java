package com.elikill58.negativity.common.protocols;

import static com.elikill58.negativity.universal.detections.keys.CheatKeys.BHOP;

import com.elikill58.negativity.api.NegativityPlayer;
import com.elikill58.negativity.api.entity.Player;
import com.elikill58.negativity.api.events.player.PlayerMoveEvent;
import com.elikill58.negativity.api.item.Materials;
import com.elikill58.negativity.api.location.Location;
import com.elikill58.negativity.api.potion.PotionEffectType;
import com.elikill58.negativity.api.protocols.Check;
import com.elikill58.negativity.api.protocols.CheckConditions;
import com.elikill58.negativity.common.protocols.data.BhopData;
import com.elikill58.negativity.universal.Negativity;
import com.elikill58.negativity.universal.detections.Cheat;
import com.elikill58.negativity.universal.report.ReportType;
import com.elikill58.negativity.universal.utils.UniversalUtils;

/**
 * Bhop (bunny hop) detector.
 *
 * <p>A vanilla jump starts with a vertical velocity of ~0.42 blocks/tick, and vanilla
 * sprint-jumping tops out around ~0.46 blocks/tick horizontally on flat ground. Holding
 * space to chain jumps is legit vanilla behaviour, so chaining alone must NOT flag.
 * What a bhop cheat adds is horizontal speed beyond what vanilla physics allows while
 * airborne. We therefore only buffer jumps whose horizontal speed exceeds the vanilla
 * sprint-jump maximum (default 0.5, above the ~0.4646 ceiling), with speed effects and
 * boosting blocks excluded, and decay the buffer on every non-matching move.
 */
public class Bhop extends Cheat {

	// Vanilla jump start dy window
	private static final double JUMP_MIN = 0.41, JUMP_MAX = 0.43;

	public Bhop() {
		super(BHOP, CheatCategory.MOVEMENT, Materials.FEATHER, BhopData::new);
	}

	@Check(name = "chained-jump", description = "Chained jumps above vanilla speed", conditions = { CheckConditions.SURVIVAL,
			CheckConditions.NO_ELYTRA, CheckConditions.NO_FLY, CheckConditions.NO_ALLOW_FLY, CheckConditions.NO_INSIDE_VEHICLE,
			CheckConditions.NO_USE_JUMP_BOOST, CheckConditions.NO_USE_SLIME, CheckConditions.NO_USE_TRIDENT, CheckConditions.NO_CLIMB_BLOCK,
			CheckConditions.NO_LIQUID_AROUND, CheckConditions.NO_ICE_AROUND, CheckConditions.NO_FIGHT })
	public void onPlayerMove(PlayerMoveEvent e, NegativityPlayer np, BhopData data) {
		Player p = e.getPlayer();
		// Speed effect raises the legit sprint-jump ceiling above our threshold
		if (p.hasPotionEffect(PotionEffectType.SPEED))
			return;
		Location from = e.getFrom(), to = e.getTo();
		double dy = to.getY() - from.getY();
		double dx = to.getX() - from.getX(), dz = to.getZ() - from.getZ();
		double horizontal = Math.sqrt(dx * dx + dz * dz);

		// above vanilla sprint-jump ceiling (~0.4646 blocks/tick on flat ground)
		double maxJumpSpeed = getConfig().getDouble("max_jump_speed", 0.5);
		boolean freshJump = dy > JUMP_MIN && dy < JUMP_MAX;
		if (freshJump && horizontal > maxJumpSpeed) {
			data.buffer++;
			int alertAt = getConfig().getInt("chained_jump_alert", 4);
			if (data.buffer >= alertAt) {
				int reliability = UniversalUtils.parseInPorcent(60 + (data.buffer - alertAt) * 5 + (horizontal - maxJumpSpeed) * 100);
				boolean mayCancel = Negativity.alertMod(ReportType.WARNING, p, this, reliability, "chained-jump",
						"Chained jumps: " + data.buffer + ", horizontal speed: " + horizontal + " > max " + maxJumpSpeed + " (jump dy: " + dy + ")") && isSetBack();
				if (mayCancel)
					e.setCancelled(true);
				data.buffer = alertAt - 1; // partial reset: keep watching without spamming
			}
		} else if (data.buffer > 0) {
			// any legit-looking move decays the buffer, so isolated boosts don't accumulate forever
			data.buffer--;
		}
	}
}
