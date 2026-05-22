package com.elikill58.negativity.common.protocols;

import java.util.EnumSet;

import com.elikill58.negativity.api.NegativityPlayer;
import com.elikill58.negativity.api.block.Block;
import com.elikill58.negativity.api.block.BlockFace;
import com.elikill58.negativity.api.entity.Player;
import com.elikill58.negativity.api.events.player.PlayerMoveEvent;
import com.elikill58.negativity.api.item.Materials;
import com.elikill58.negativity.api.location.Location;
import com.elikill58.negativity.api.protocols.Check;
import com.elikill58.negativity.api.protocols.CheckConditions;
import com.elikill58.negativity.common.protocols.data.GroundSpoofData;
import com.elikill58.negativity.universal.MinecraftConstants;
import com.elikill58.negativity.universal.Negativity;
import com.elikill58.negativity.universal.detections.Cheat;
import com.elikill58.negativity.universal.detections.keys.CheatKeys;
import com.elikill58.negativity.universal.report.ReportType;

public class GroundSpoof extends Cheat {
	private static final EnumSet<BlockFace> SUPPORTED_FACES = EnumSet.of(BlockFace.WEST, BlockFace.EAST, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.NORTH_WEST, BlockFace.SOUTH_WEST,
			BlockFace.NORTH_EAST, BlockFace.SOUTH_EAST);

	public GroundSpoof() {
		super(CheatKeys.GROUND_SPOOF, CheatCategory.MOVEMENT, Materials.STONE, GroundSpoofData::new);
	}

	@Check(name = "check-blocks-under", description = "Block under player have to be considered as ground", conditions = { CheckConditions.SURVIVAL, CheckConditions.GROUND,
			CheckConditions.NO_SNEAK })
	public void onGroundSpoof(PlayerMoveEvent e, NegativityPlayer np, GroundSpoofData data) {
		Player p = e.getPlayer();
		if (e.isCancelled() || !p.isOnGround()) {// cancelled or player say he is not on ground
			data.wasAlert = false;
			return;
		}
		if (isOnGround(e.getTo()) || p.getFallDistance() > 3 || p.getFallDistance() > p.getWalkSpeed()) {
			data.wasAlert = false;
			return;
		}
		Block block = e.getTo().getBlock();
		Block downBlock = block.getRelative(BlockFace.DOWN);
		if (blockJustAroundAreNotAir(block) || blockJustAroundAreNotAir(downBlock)) {
			data.wasAlert = false;
			return;
		}
		double diffY = e.getTo().getY() - e.getFrom().getY();
		float fallDistance = p.getFallDistance();
		if (diffY <= p.getWalkSpeed()
				// MC produces step-up Y-deltas in the 0.164773... family across versions (1.20.5+):
				// observed exact doubles include 0.1647732818260721 and 0.16477327999999858.
				// Use a tolerance instead of strict equality to absorb new FP-noise variants without
				// another patch each time.
				|| Math.abs(diffY - 0.16477328) < 1.0e-6
				// Half-block step (slab/stair edge) during a one-tick free-fall transient: server still
				// reports onGround=true while motion.y has decremented by exactly GRAVITY for one tick.
				// Repeatedly fired ~9 times in a row on normal sprint over slabs in 1.21.8.
				|| (diffY == 0.5 && fallDistance <= MinecraftConstants.GRAVITY + 1e-6)
				// Legit auto step-up (0.5 pre-1.20.5, 0.6 since); height is in MinecraftConstants.
				|| diffY == MinecraftConstants.stepHeight(p.getPlayerVersion()))
			return;
		if (data.wasAlert)
			Negativity.alertMod(ReportType.WARNING, p, this, getReliability(p), "check-blocks-under",
					"Air BlockFaces: " + getAirBlocks(p).toString() + ", fall: " + p.getFallDistance() + ", sneaking: " + p.isSneaking() + ", Y diff: " + diffY,
					new CheatHover.Literal("Ground Spoof (Fly, NoFall, and other movement hacks)"));
		else
			data.wasAlert = true;
	}

	private boolean blockJustAroundAreNotAir(Block block) {
		return isNotAir(block) || isNotAir(block.getRelative(BlockFace.NORTH)) || isNotAir(block.getRelative(BlockFace.SOUTH)) || isNotAir(block.getRelative(BlockFace.EAST))
				|| isNotAir(block.getRelative(BlockFace.WEST));
	}

	private static boolean isNotAir(Block block) {
		return !block.getType().equals(Materials.AIR);
	}

	public static boolean isOnGround(Location to) {
		return isOnGroundForBlock(to, to.getBlock()) || isOnGroundForBlock(to, to.getBlock().getRelative(BlockFace.DOWN));
	}

	public static boolean isOnGroundForBlock(Location to, Block block) {
		if (isNotAir(block)) {
			return true;
		}
		for (final BlockFace face : SUPPORTED_FACES) {
			if (isNotAir(block.getRelative(face)) && isSupportedBy(to, face)) {
				return true;
			}
		}
		return false;
	}

	private int getReliability(Player player) {
		return 85 + getAirBlocks(player).size();
	}

	private EnumSet<BlockFace> getAirBlocks(Player player) {
		final Block block = player.getLocation().getBlock();
		final Block downBlock = block.getRelative(BlockFace.DOWN);

		if (isNotAir(downBlock)) {
			return EnumSet.noneOf(BlockFace.class);
		}

		final EnumSet<BlockFace> faces = EnumSet.noneOf(BlockFace.class);
		for (final BlockFace face : SUPPORTED_FACES) {
			if (isNotAir(downBlock.getRelative(face))) {
				continue;
			}
			faces.add(face);
		}
		return faces;
	}

	public static boolean isSupportedBy(final Location playerLoc, final BlockFace face) {
		switch (face) {
		case NORTH:
			final double northRequiredZP = 0.31;
			final double northRequiredZN = 0.69;
			final double northPlayerZ = Math.abs(playerLoc.getZ() - ((int) playerLoc.getZ()));
			if (playerLoc.getZ() < 0) {
				return northPlayerZ >= northRequiredZN;
			}
			return northPlayerZ <= northRequiredZP;
		case EAST:
			final double eastRequiredXP = 0.69;
			final double eastRequiredXN = 0.31;
			final double eastPlayerX = Math.abs(playerLoc.getX() - ((int) playerLoc.getX()));
			if (playerLoc.getX() < 0) {
				return eastPlayerX <= eastRequiredXN;
			}
			return eastPlayerX >= eastRequiredXP;
		case SOUTH:
			final double southRequiredZP = 0.69;
			final double southRequiredZN = 0.31;
			final double southPlayerZ = Math.abs(playerLoc.getZ() - ((int) playerLoc.getZ()));
			if (playerLoc.getZ() < 0) {
				return southPlayerZ <= southRequiredZN;
			}
			return southPlayerZ >= southRequiredZP;
		case WEST:
			final double westRequiredXP = 0.31;
			final double westRequiredXN = 0.69;
			final double westPlayerX = Math.abs(playerLoc.getX() - ((int) playerLoc.getX()));
			if (playerLoc.getX() < 0) {
				return westPlayerX >= westRequiredXN;
			}
			return westPlayerX <= westRequiredXP;
		case NORTH_EAST:
			return isSupportedBy(playerLoc, BlockFace.NORTH) || isSupportedBy(playerLoc, BlockFace.EAST);
		case SOUTH_EAST:
			return isSupportedBy(playerLoc, BlockFace.SOUTH) || isSupportedBy(playerLoc, BlockFace.EAST);
		case SOUTH_WEST:
			return isSupportedBy(playerLoc, BlockFace.SOUTH) || isSupportedBy(playerLoc, BlockFace.WEST);
		case NORTH_WEST:
			return isSupportedBy(playerLoc, BlockFace.NORTH) || isSupportedBy(playerLoc, BlockFace.WEST);
		default:
			return false;
		}
	}
}