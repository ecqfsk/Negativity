package com.elikill58.negativity.common.protocols;

import static com.elikill58.negativity.universal.detections.keys.CheatKeys.BHOP;

import com.elikill58.negativity.api.NegativityPlayer;
import com.elikill58.negativity.api.entity.Player;
import com.elikill58.negativity.api.events.player.PlayerMoveEvent;
import com.elikill58.negativity.api.item.Materials;
import com.elikill58.negativity.api.location.Location;
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
 * <p>A vanilla jump imparts an initial vertical velocity of ~0.42 blocks/tick. A player
 * "bunny hopping" chains fresh jumps together while keeping a high horizontal speed,
 * effectively never settling on the ground between jumps. We buffer suspicious jumps
 * (fresh jump velocity + high horizontal move) and decay the buffer on legit ground moves.
 */
public class Bhop extends Cheat {

	// Horizontal distance per tick above which a chained jump is suspicious
	private static final double HORIZONTAL_THRESHOLD = 0.32;
	// Vanilla jump start dy window
	private static final double JUMP_MIN = 0.41, JUMP_MAX = 0.43;

	public Bhop() {
		super(BHOP, CheatCategory.MOVEMENT, Materials.FEATHER, BhopData::new);
	}

	@Check(name = "chained-jump", description = "Chained jumps keeping high speed", conditions = { CheckConditions.SURVIVAL,
			CheckConditions.NO_ELYTRA, CheckConditions.NO_FLY, CheckConditions.NO_ALLOW_FLY, CheckConditions.NO_INSIDE_VEHICLE,
			CheckConditions.NO_USE_JUMP_BOOST, CheckConditions.NO_USE_SLIME, CheckConditions.NO_CLIMB_BLOCK, CheckConditions.NO_LIQUID_AROUND })
	public void onPlayerMove(PlayerMoveEvent e, NegativityPlayer np, BhopData data) {
		Player p = e.getPlayer();
		Location from = e.getFrom(), to = e.getTo();
		double dy = to.getY() - from.getY();
		double dx = to.getX() - from.getX(), dz = to.getZ() - from.getZ();
		double horizontal = Math.sqrt(dx * dx + dz * dz);

		boolean freshJump = dy > JUMP_MIN && dy < JUMP_MAX;
		if (freshJump && horizontal > HORIZONTAL_THRESHOLD) {
			data.buffer++;
			int alertAt = getConfig().getInt("chained_jump_alert", 4);
			if (data.buffer >= alertAt) {
				int reliability = UniversalUtils.parseInPorcent(55 + (data.buffer - alertAt) * 8 + horizontal * 30);
				boolean mayCancel = Negativity.alertMod(ReportType.WARNING, p, this, reliability, "chained-jump",
						"Chained jumps: " + data.buffer + ", horizontal speed: " + horizontal + " (jump dy: " + dy + ")") && isSetBack();
				if (mayCancel)
					e.setCancelled(true);
			}
		} else if (dy <= 0 && horizontal < 0.05) {
			// standing / landing: decay
			if (data.buffer > 0)
				data.buffer--;
		}
	}
}
