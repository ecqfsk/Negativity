package com.elikill58.negativity.common.violation;

/**
 * Per-check thresholds loaded from configuration.
 */
public record CheckThresholds(
        double alertVl,
        double setbackVl,
        double punishVl,
        double bufferDecayPerSecond,
        double bufferMax,
        long alertCooldownMs,
        double violationWeight,
        boolean cancel,
        boolean setback,
        boolean logOnly
) {
    public static CheckThresholds defaults() {
        return new CheckThresholds(
                5.0,
                10.0,
                25.0,
                0.5,
                50.0,
                1000L,
                1.0,
                false,
                true,
                false
        );
    }
}
