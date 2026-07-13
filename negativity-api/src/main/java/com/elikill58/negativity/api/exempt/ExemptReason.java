package com.elikill58.negativity.api.exempt;

/**
 * Why a detection was skipped. Used for debug output and third-party integrations.
 */
public enum ExemptReason {
    JOIN("join"),
    TELEPORT("teleport"),
    RESPAWN("respawn"),
    WORLD_CHANGE("world_change"),
    VEHICLE("vehicle"),
    KNOCKBACK("knockback"),
    EXPLOSION("explosion"),
    PISTON("piston"),
    SLIME("slime"),
    HONEY("honey"),
    WATER("water"),
    LAVA("lava"),
    CLIMBABLE("climbable"),
    WEB("web"),
    ELYTRA("elytra"),
    RIPTIDE("riptide"),
    LEVITATION("levitation"),
    SLOW_FALLING("slow_falling"),
    FLIGHT_ALLOWED("flight_allowed"),
    CREATIVE_OR_SPECTATOR("creative_or_spectator"),
    LOW_TPS("low_tps"),
    HIGH_LATENCY("high_latency"),
    PACKET_LOSS("packet_loss"),
    PLUGIN("plugin"),
    MANUAL("manual"),
    BEDROCK("bedrock"),
    PERMISSION("permission"),
    RELOAD("reload"),
    OTHER("other");

    private final String id;

    ExemptReason(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }
}
