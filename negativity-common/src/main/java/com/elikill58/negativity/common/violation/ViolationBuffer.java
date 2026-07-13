package com.elikill58.negativity.common.violation;

/**
 * Progressive violation buffer with linear decay.
 * Thread-safe for concurrent flag + tick decay.
 */
public final class ViolationBuffer {

    private double value;
    private long lastUpdateMs;
    private final double decayPerSecond;
    private final double maxValue;

    public ViolationBuffer(double decayPerSecond, double maxValue) {
        if (decayPerSecond < 0) {
            throw new IllegalArgumentException("decayPerSecond must be >= 0");
        }
        if (maxValue <= 0) {
            throw new IllegalArgumentException("maxValue must be > 0");
        }
        this.decayPerSecond = decayPerSecond;
        this.maxValue = maxValue;
        this.lastUpdateMs = System.currentTimeMillis();
    }

    public synchronized double get() {
        applyDecay(System.currentTimeMillis());
        return value;
    }

    /**
     * Adds {@code amount} after applying pending decay.
     *
     * @return buffer value after the addition
     */
    public synchronized double add(double amount) {
        long now = System.currentTimeMillis();
        applyDecay(now);
        if (amount > 0) {
            value = Math.min(maxValue, value + amount);
        }
        lastUpdateMs = now;
        return value;
    }

    public synchronized double reduce(double amount) {
        long now = System.currentTimeMillis();
        applyDecay(now);
        value = Math.max(0, value - Math.max(0, amount));
        lastUpdateMs = now;
        return value;
    }

    public synchronized void reset() {
        value = 0;
        lastUpdateMs = System.currentTimeMillis();
    }

    public double decayPerSecond() {
        return decayPerSecond;
    }

    public double maxValue() {
        return maxValue;
    }

    private void applyDecay(long nowMs) {
        if (value <= 0 || decayPerSecond <= 0) {
            lastUpdateMs = nowMs;
            return;
        }
        long delta = nowMs - lastUpdateMs;
        if (delta <= 0) {
            return;
        }
        double decay = (delta / 1000.0) * decayPerSecond;
        value = Math.max(0, value - decay);
        lastUpdateMs = nowMs;
    }
}
