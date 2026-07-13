package com.elikill58.negativity.common.packet;

/**
 * Platform-agnostic packet categories used by checks.
 */
public enum PacketKind {
    FLYING,
    POSITION,
    LOOK,
    POSITION_LOOK,
    ARM_ANIMATION,
    USE_ENTITY,
    ENTITY_ACTION,
    BLOCK_DIG,
    BLOCK_PLACE,
    WINDOW_CLICK,
    HELD_ITEM,
    STEER_VEHICLE,
    ABILITIES,
    CUSTOM_PAYLOAD,
    TRANSACTION,
    KEEP_ALIVE,
    OTHER
}
