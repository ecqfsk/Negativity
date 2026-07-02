package com.elikill58.negativity.common.protocols;

import static com.elikill58.negativity.universal.detections.keys.CheatKeys.BOAT_FLY;

import com.elikill58.negativity.api.NegativityPlayer;
import com.elikill58.negativity.api.entity.Entity;
import com.elikill58.negativity.api.entity.EntityType;
import com.elikill58.negativity.api.entity.Player;
import com.elikill58.negativity.api.events.packets.PacketPreReceiveEvent;
import com.elikill58.negativity.api.item.Materials;
import com.elikill58.negativity.api.location.Location;
import com.elikill58.negativity.api.packets.PacketType;
import com.elikill58.negativity.api.protocols.Check;
import com.elikill58.negativity.api.protocols.CheckConditions;
import com.elikill58.negativity.common.protocols.data.BoatFlyData;
import com.elikill58.negativity.universal.Negativity;
import com.elikill58.negativity.universal.detections.Cheat;
import com.elikill58.negativity.universal.report.ReportType;
import com.elikill58.negativity.universal.utils.UniversalUtils;

/**
 * BoatFly detector.
 *
 * <p>While riding a boat the client reports position through vehicle packets, not regular
 * flying packets, so this check runs on the vehicle-steer packet (like Speed's
 * distance-vehicle). A boat with nothing under it (no water, ground, lily pad or bubble
 * column within 2 blocks — "nothing" meaning strictly air) must fall ~0.08 blocks/tick.
 * A boat that stays airborne without falling, or gains height, is flown. Checking against
 * block support instead of raw upward motion avoids flagging bubble columns and waves.
 */
public class BoatFly extends Cheat {

	public BoatFly() {
		super(BOAT_FLY, CheatCategory.MOVEMENT, Materials.CHEST, BoatFlyData::new);
	}

	@Check(name = "boat-ascend", description = "Boat staying airborne or gaining height", conditions = { CheckConditions.INSIDE_VEHICLE,
			CheckConditions.NO_ALLOW_FLY })
	public void onVehicleMove(PacketPreReceiveEvent e, NegativityPlayer np, BoatFlyData data) {
		if (!e.getPacket().getPacketType().equals(PacketType.Client.STEER_VEHICLE))
			return;
		Player p = e.getPlayer();
		Entity vehicle = p.getVehicle();
		if (vehicle == null || !vehicle.getType().equals(EntityType.BOAT))
			return;
		Location loc = vehicle.getLocation();
		// supported = anything but air at, 1 or 2 blocks below the boat
		// (water, ground, lily pads and bubble columns all reset the counter)
		boolean supported = !loc.getBlock().getType().equals(Materials.AIR)
				|| !loc.clone().sub(0, 1, 0).getBlock().getType().equals(Materials.AIR)
				|| !loc.clone().sub(0, 2, 0).getBlock().getType().equals(Materials.AIR);
		if (supported) {
			data.airTicks = 0;
			data.startY = loc.getY();
			return;
		}
		if (data.airTicks == 0)
			data.startY = loc.getY();
		data.airTicks++;

		int alertAt = getConfig().getInt("air_ticks_alert", 12);
		if (data.airTicks > alertAt) {
			double heightGained = loc.getY() - data.startY;
			// an unsupported boat falls ~0.08 blocks/tick: barely falling = held in the air
			double expectedFall = (data.airTicks - alertAt) * 0.08;
			double actualFall = data.startY - loc.getY();
			if (heightGained > 0.1 || actualFall < expectedFall * 0.4) {
				int reliability = UniversalUtils.parseInPorcent(75 + data.airTicks / 2);
				boolean mayCancel = Negativity.alertMod(ReportType.WARNING, p, this, reliability, "boat-ascend",
						"Boat in the air for " + data.airTicks + " ticks, height gained: " + heightGained
								+ ", fall: " + actualFall + " (expected: " + expectedFall + ")") && isSetBack();
				if (mayCancel)
					e.setCancelled(true);
				data.airTicks = alertAt; // partial reset: keep watching without spamming
			}
		}
	}
}
