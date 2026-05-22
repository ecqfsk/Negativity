package com.elikill58.negativity.universal;

/**
 * Centralized Minecraft physics & game constants.
 *
 * Values are taken directly from the vanilla Minecraft source (CraftBukkit / NMS).
 * Where a constant differs between versions, a {@code forVersion(Version)} helper
 * is provided to return the correct value for the player's protocol version.
 *
 * <p>Anti-cheat checks should reference these constants instead of duplicating
 * literal magic numbers. Tuning thresholds that are NOT vanilla Minecraft values
 * (false-positive patches, buffer counts, ad-hoc heuristics) do NOT belong here.
 */
public final class MinecraftConstants {

	private MinecraftConstants() {}

	// ---------- Vertical motion (EntityLiving / EntityHuman) ----------

	/** Gravity applied each tick to entities: {@code EntityLiving.DEFAULT_BASE_GRAVITY}. */
	public static final double GRAVITY = 0.08D;

	/** Y-axis air drag (multiplier per tick on deltaMovement.y): {@code EntityLiving.INPUT_FRICTION}. */
	public static final float Y_DRAG = 0.98F;

	/** Initial vertical velocity for a jump from solid ground: {@code EntityLiving.BASE_JUMP_POWER}. */
	public static final float JUMP_POWER = 0.42F;

	/** Sprint horizontal impulse added on jump (sin/cos * this): {@code EntityLiving.jumpFromGround}. */
	public static final double SPRINT_JUMP_BOOST = 0.2D;

	/** Per-amplifier bonus jump velocity from Jump Boost effect: {@code EntityLiving.getJumpBoostPower}. */
	public static final float JUMP_BOOST_PER_LEVEL = 0.1F;

	/** Minimum movement before the server registers a change: {@code EntityLiving.MIN_MOVEMENT_DISTANCE}. */
	public static final double MIN_MOVEMENT_DISTANCE = 0.003D;

	// ---------- Horizontal motion / friction ----------

	/** Horizontal air friction multiplier per tick: see {@code EntityLiving.travel}. */
	public static final float AIR_FRICTION_XZ = 0.91F;

	/** Default block top friction (most blocks): {@code BlockBase.Info.friction}. */
	public static final float DEFAULT_BLOCK_FRICTION = 0.6F;

	/** Ice / packed ice / blue ice friction. */
	public static final float ICE_FRICTION = 0.98F;

	/** Slime block top friction. */
	public static final float SLIME_FRICTION = 0.8F;

	/** Honey block top friction. */
	public static final float HONEY_FRICTION = 0.4F;

	/**
	 * Walk-speed normalization factor used by {@code EntityLiving.getFrictionInfluencedSpeed}:
	 * <pre>speed * (NORMALIZER / friction^3)</pre>
	 * In MC <=1.20 it was {@code 0.16277136F} (= 0.6^3 nominally).
	 * In MC 1.21+ it became {@code 0.21600002F}.
	 */
	public static float walkSpeedNormalizer(Version v) {
		return v.isNewerOrEquals(Version.V1_21) ? 0.21600002F : 0.16277136F;
	}

	/** Legacy walk-speed normalizer (pre-1.21). Kept for code that doesn't need version awareness. */
	public static final float WALK_SPEED_NORMALIZER_LEGACY = 0.16277136F;

	/** Base air movement factor (applied in {@code travel} when not on ground). */
	public static final float AIR_MOVE_FACTOR = 0.02F;

	/**
	 * Base sprint speed bonus (multiplied operation on movement speed attribute):
	 * {@code EntityLiving.SPEED_MODIFIER_SPRINTING} = +30%.
	 */
	public static final double SPRINT_SPEED_MULTIPLIER = 0.3D;

	// ---------- Player ground packet resolution ----------

	/**
	 * Server-side ground-position quantum: the Y coordinate of an on-ground packet
	 * is always a multiple of this (1/64). See {@code PlayerConnection} ground checks.
	 */
	public static final double GROUND_POSITION_QUANTUM = 0.015625D;

	// ---------- Step / collision ----------

	/**
	 * Maximum step-up height for the player:
	 * <ul>
	 *   <li>1.7 – 1.20.4: {@code 0.5F}</li>
	 *   <li>1.20.5+: {@code 0.6F} (raised when player step-up was added to the {@code STEP_HEIGHT} attribute)</li>
	 * </ul>
	 */
	public static float stepHeight(Version v) {
		return v.isNewerOrEquals(Version.V1_20_6) ? 0.6F : 0.5F;
	}

	// ---------- Reach (combat / block interaction) ----------

	/** Default block-interaction reach for the player: {@code EntityHuman.DEFAULT_BLOCK_INTERACTION_RANGE}. */
	public static final float BLOCK_REACH_SURVIVAL = 4.5F;

	/** Default entity-interaction reach for the player: {@code EntityHuman.DEFAULT_ENTITY_INTERACTION_RANGE}. */
	public static final float ENTITY_REACH_SURVIVAL = 3.0F;

	/** Extra reach granted in creative gamemode (added to base block reach). */
	public static final float CREATIVE_REACH_BONUS = 1.5F;

	/**
	 * Maximum allowed reach to an entity from packet-side, with a small leniency
	 * for lag / hitbox edges. Used as a hard upper bound by Reach check.
	 */
	public static double entityReachLimit(Version v, boolean creative) {
		// 1.9+ removed the 1.8-era short attack range; survival is the 3.0 attribute value.
		double base = ENTITY_REACH_SURVIVAL;
		return creative ? base + 3.0D : base;
	}

	// ---------- Water / lava drag ----------

	/** Horizontal+Y drag multiplier per tick while submerged in water: {@code Entity.WATER_DRAG}. */
	public static final float WATER_DRAG = 0.8F;

	/** Y bobbing impulse on entering water: {@code EntityLiving.WATER_FLOAT_IMPULSE}. */
	public static final float WATER_FLOAT_IMPULSE = 0.04F;

	// ---------- Potion effect amplitudes ----------

	/** Speed effect multiplicative bonus per amplifier level. */
	public static final double SPEED_EFFECT_PER_LEVEL = 0.20D;

	/** Slowness effect multiplicative penalty per amplifier level. */
	public static final double SLOWNESS_EFFECT_PER_LEVEL = -0.15D;

	/**
	 * Levitation per-tick Y-velocity adjustment (1.9+):
	 * <pre>vel.y += (0.05 * (amplifier + 1) - vel.y) * 0.2</pre>
	 * Constants used in the formula.
	 */
	public static final double LEVITATION_FACTOR = 0.05D;
	public static final double LEVITATION_LERP = 0.2D;

	// ---------- Helpers ----------

	/**
	 * Whether a Y coordinate from a flying packet lines up with the on-ground quantum.
	 * <p>Mirrors the server-side check that {@code y % 1/64 == 0} for ground packets.
	 */
	public static boolean isGroundQuantized(double y) {
		return y % GROUND_POSITION_QUANTUM == 0.0D;
	}
}
