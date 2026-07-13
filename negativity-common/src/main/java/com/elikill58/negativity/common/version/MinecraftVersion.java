package com.elikill58.negativity.common.version;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Optional;

/**
 * Central Minecraft version registry. Version-specific behaviour must go through
 * adapters that consult this enum — never scatter {@code if (version >= ...)} in checks.
 */
public enum MinecraftVersion {
    V1_8(8, 47),
    V1_9(9, 110),
    V1_10(10, 210),
    V1_11(11, 316),
    V1_12(12, 340),
    V1_13(13, 404),
    V1_14(14, 498),
    V1_15(15, 578),
    V1_16(16, 754),
    V1_17(17, 756),
    V1_18(18, 758),
    V1_19(19, 762),
    V1_20(20, 766),
    V1_20_5(20, 771),
    V1_21(21, 767),
    V1_21_1(21, 768),
    V1_21_2(21, 769),
    V1_21_3(21, 770),
    V1_21_4(21, 769), // protocol can vary by patch; data version is preferred when available
    V1_21_5(21, 774),
    V1_21_6(21, 775),
    V1_21_7(21, 776),
    V1_21_8(21, 777),
    V1_21_9(21, 778),
    V1_21_10(21, 779),
    V1_21_11(21, 780),
    UNKNOWN(0, -1);

    private final int majorMinor; // 8 = 1.8, 21 = 1.21 family
    private final int protocolHint;

    MinecraftVersion(int majorMinor, int protocolHint) {
        this.majorMinor = majorMinor;
        this.protocolHint = protocolHint;
    }

    public int majorMinor() {
        return majorMinor;
    }

    public int protocolHint() {
        return protocolHint;
    }

    public boolean isAtLeast(@NotNull MinecraftVersion other) {
        return this.ordinal() >= other.ordinal() && this != UNKNOWN;
    }

    public boolean isLegacy() {
        return majorMinor > 0 && majorMinor < 13;
    }

    public boolean supportsModernPaper() {
        // Paper 1.20.5+ remapped / no craftbukkit package version
        return isAtLeast(V1_20_5);
    }

    @NotNull
    public static MinecraftVersion fromBukkitVersion(@NotNull String bukkitVersion) {
        // Examples: "1.21.4-R0.1-SNAPSHOT", "1.20.4-R0.1-SNAPSHOT"
        String normalized = bukkitVersion.toLowerCase(Locale.ROOT);
        if (normalized.startsWith("1.21.11")) return V1_21_11;
        if (normalized.startsWith("1.21.10")) return V1_21_10;
        if (normalized.startsWith("1.21.9")) return V1_21_9;
        if (normalized.startsWith("1.21.8")) return V1_21_8;
        if (normalized.startsWith("1.21.7")) return V1_21_7;
        if (normalized.startsWith("1.21.6")) return V1_21_6;
        if (normalized.startsWith("1.21.5")) return V1_21_5;
        if (normalized.startsWith("1.21.4")) return V1_21_4;
        if (normalized.startsWith("1.21.3")) return V1_21_3;
        if (normalized.startsWith("1.21.2")) return V1_21_2;
        if (normalized.startsWith("1.21.1")) return V1_21_1;
        if (normalized.startsWith("1.21")) return V1_21;
        if (normalized.startsWith("1.20.6") || normalized.startsWith("1.20.5")) return V1_20_5;
        if (normalized.startsWith("1.20")) return V1_20;
        if (normalized.startsWith("1.19")) return V1_19;
        if (normalized.startsWith("1.18")) return V1_18;
        if (normalized.startsWith("1.17")) return V1_17;
        if (normalized.startsWith("1.16")) return V1_16;
        if (normalized.startsWith("1.15")) return V1_15;
        if (normalized.startsWith("1.14")) return V1_14;
        if (normalized.startsWith("1.13")) return V1_13;
        if (normalized.startsWith("1.12")) return V1_12;
        if (normalized.startsWith("1.11")) return V1_11;
        if (normalized.startsWith("1.10")) return V1_10;
        if (normalized.startsWith("1.9")) return V1_9;
        if (normalized.startsWith("1.8")) return V1_8;
        return UNKNOWN;
    }

    @NotNull
    public static Optional<MinecraftVersion> fromProtocol(int protocol) {
        MinecraftVersion best = null;
        for (MinecraftVersion v : values()) {
            if (v.protocolHint == protocol) {
                return Optional.of(v);
            }
            if (v.protocolHint > 0 && v.protocolHint <= protocol) {
                best = v;
            }
        }
        return Optional.ofNullable(best);
    }

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT).replace('_', '.');
    }
}
