package com.elikill58.negativity.common.protocols;

import static com.elikill58.negativity.universal.detections.keys.CheatKeys.XRAY;

import java.util.Arrays;
import java.util.List;

import com.elikill58.negativity.api.NegativityPlayer;
import com.elikill58.negativity.api.block.Block;
import com.elikill58.negativity.api.entity.Player;
import com.elikill58.negativity.api.events.block.BlockBreakEvent;
import com.elikill58.negativity.api.item.Material;
import com.elikill58.negativity.api.item.Materials;
import com.elikill58.negativity.api.location.Location;
import com.elikill58.negativity.api.location.Vector;
import com.elikill58.negativity.api.protocols.Check;
import com.elikill58.negativity.api.ray.RayResult;
import com.elikill58.negativity.api.ray.block.BlockRayBuilder;
import com.elikill58.negativity.api.ray.block.BlockRayResult;
import com.elikill58.negativity.common.protocols.data.XRayData;
import com.elikill58.negativity.universal.Minerate;
import com.elikill58.negativity.universal.Minerate.MinerateType;
import com.elikill58.negativity.universal.Negativity;
import com.elikill58.negativity.universal.account.NegativityAccount;
import com.elikill58.negativity.universal.detections.Cheat;
import com.elikill58.negativity.universal.report.ReportType;
import com.elikill58.negativity.universal.storage.account.NegativityAccountStorage;
import com.elikill58.negativity.universal.utils.UniversalUtils;

public class XRay extends Cheat {

	private static final List<Material> ORES = Arrays.asList(Materials.COAL_ORE, Materials.IRON_ORE, Materials.GOLD_ORE,
			Materials.DIAMOND_ORE, Materials.EMERALD_ORE, Materials.REDSTONE_ORE, Materials.QUARTZ_ORE,
			Materials.LAPIS_ORE);
	private static final List<Material> IMPORTANT_ORES = Arrays.asList(Materials.GOLD_ORE, Materials.DIAMOND_ORE,
			Materials.EMERALD_ORE, Materials.REDSTONE_ORE, Materials.QUARTZ_ORE, Materials.LAPIS_ORE);
	private static final List<Material> MINING_BLOCK = Arrays.asList(Materials.STONE, Materials.ANDESITE,
			Materials.GRANITE, Materials.DIORITE, Materials.GRAVEL);
	private static final long TIME_MINING = 10000;

	public XRay() {
		super(XRAY, CheatCategory.WORLD, Materials.EMERALD_ORE, XRayData::new, CheatDescription.BLOCKS);
	}

	@Check(name = "minerate", description = "Count minerate")
	public void onBlockBreakMinerate(BlockBreakEvent e) {
		Player p = e.getPlayer();
		NegativityAccount acc = NegativityAccount.get(p.getUniqueId());
		Minerate mine = acc.getMinerate();
		MinerateType type = MinerateType.getMinerateType(e.getBlock().getType().getId());
		mine.addMine(type, p);
		if(type == null)
			return;
		int minedType = 0, fullMined = mine.getFullMined();
		if (fullMined <= 0)
			return;
		for (int i : mine.getMined().values())
			minedType += i;
		// percentage of ores among all mined blocks (integer division alone would always give 0 or 1)
		int relia = minedType * 100 / fullMined;
		Negativity.alertMod(ReportType.WARNING, p, this, relia, "minerate",
				type.getOreName() + " mined. Full mined: " + fullMined + ". Mined by type: " + mine,
				hoverMsg("main", "%name%", type.getName(), "%nb%", mine.getMinerateType(type)));
		NegativityAccountStorage.getStorage().saveAccount(acc);
	}

	/**
	 * Lucky-turn detection: a legit miner digs blind, so when his tunnel changes direction
	 * the odds that the new direction points at a HIDDEN rare ore are low. An xray user sees
	 * ores through the walls and turns precisely toward them, so nearly every direction
	 * change is "lucky". We track the digging direction from consecutively broken blocks,
	 * and on each significant turn we ray-cast along the new direction looking for an
	 * important ore that is not visible (hidden behind other blocks). Alert when enough
	 * turns were made and the lucky ratio is beyond what chance explains.
	 */
	@Check(name = "mining-direction", description = "Suspiciously lucky mining turns toward hidden rare ores")
	public void onBlockBreak(BlockBreakEvent e, NegativityPlayer np, XRayData data) {
		Player p = e.getPlayer();
		Block b = e.getBlock();
		Material type = b.getType();
		if (!MINING_BLOCK.contains(type) && !ORES.contains(type))
			return; // not underground mining
		long time = System.currentTimeMillis();
		Location loc = b.getLocation();
		boolean sameSession = (time - data.mining) < TIME_MINING && data.lastBreakLoc != null
				&& data.lastBreakLoc.getWorld().getName().equals(loc.getWorld().getName())
				&& data.lastBreakLoc.distance(loc) < 3;
		data.mining = time;
		if (!sameSession) {
			// new tunnel (or teleport/world change): restart direction tracking
			data.lastBreakLoc = loc;
			data.hasDir = false;
			return;
		}

		double dx = loc.getX() - data.lastBreakLoc.getX(), dy = loc.getY() - data.lastBreakLoc.getY(),
				dz = loc.getZ() - data.lastBreakLoc.getZ();
		double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
		data.lastBreakLoc = loc;
		if (length == 0)
			return;
		dx /= length;
		dy /= length;
		dz /= length;
		if (!data.hasDir) {
			data.dirX = dx;
			data.dirY = dy;
			data.dirZ = dz;
			data.hasDir = true;
			return;
		}
		// cos of the angle between the previous and the new digging direction
		double cos = dx * data.dirX + dy * data.dirY + dz * data.dirZ;
		data.dirX = dx;
		data.dirY = dy;
		data.dirZ = dz;
		if (cos >= getConfig().getDouble("checks.mining-direction.turn_cos", 0.7))
			return; // still digging (roughly) straight: turns are the signal, not the tunnel

		data.totalTurns++;
		// does the NEW direction point at a rare ore hidden behind other blocks?
		int luckyDistance = getConfig().getInt("checks.mining-direction.lucky_distance", 6);
		BlockRayResult ray = new BlockRayBuilder(loc.clone().add(0.5, 0.5, 0.5), new Vector(dx, dy, dz))
				.neededType(IMPORTANT_ORES.toArray(new Material[0])).maxDistance(luckyDistance).build().compile();
		boolean lucky = ray.getBlock() != null && ray.getRayResult().equals(RayResult.NEEDED_FOUND)
				&& ray.hasBlockExceptSearched(); // hidden = the ore was NOT visible from the tunnel
		if (lucky)
			data.luckyTurns++;

		int minTurns = getConfig().getInt("checks.mining-direction.min_turns", 5);
		if (data.totalTurns < minTurns)
			return; // sample too small to say anything about luck
		int ratio = data.luckyTurns * 100 / data.totalTurns;
		if (ratio >= getConfig().getInt("checks.mining-direction.lucky_percent", 60)) {
			Negativity.alertMod(ReportType.WARNING, p, this, UniversalUtils.parseInPorcent(ratio), "mining-direction",
					"Lucky turns: " + data.luckyTurns + "/" + data.totalTurns + " (" + ratio + "%)"
							+ (lucky ? ", last found: " + ray.getType().getId() : ""),
					hoverMsg("main", "%name%", lucky ? ray.getType().getId() : "?", "%nb%", data.luckyTurns));
			// halve instead of clearing: keep watching without spamming every following turn
			data.luckyTurns /= 2;
			data.totalTurns /= 2;
		}
	}
}
