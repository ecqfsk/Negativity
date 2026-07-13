package com.elikill58.negativity.common.latency;

/**
 * Short connection history for a single player.
 * Does not globally disable checks — consumers apply per-check tolerances.
 */
public final class LatencyTracker {

    private final int capacity;
    private final int[] pingSamples;
    private int index;
    private int size;
    private int lastPing;
    private double jitter;
    private long lastPacketMs;
    private int suspectedLoss;

    public LatencyTracker(int capacity) {
        if (capacity < 3) {
            throw new IllegalArgumentException("capacity must be >= 3");
        }
        this.capacity = capacity;
        this.pingSamples = new int[capacity];
        this.lastPacketMs = System.currentTimeMillis();
    }

    public synchronized void recordPing(int pingMs) {
        if (pingMs < 0) {
            pingMs = 0;
        }
        lastPing = pingMs;
        pingSamples[index] = pingMs;
        index = (index + 1) % capacity;
        if (size < capacity) {
            size++;
        }
        jitter = computeJitter();
    }

    public synchronized void recordPacket() {
        long now = System.currentTimeMillis();
        long gap = now - lastPacketMs;
        // rough loss/spike heuristic: large gap relative to ping
        if (lastPing > 0 && gap > Math.max(250L, lastPing * 4L)) {
            suspectedLoss++;
        } else if (suspectedLoss > 0) {
            suspectedLoss--;
        }
        lastPacketMs = now;
    }

    public synchronized int ping() {
        return lastPing;
    }

    public synchronized double averagePing() {
        if (size == 0) {
            return lastPing;
        }
        long sum = 0;
        for (int i = 0; i < size; i++) {
            sum += pingSamples[i];
        }
        return sum / (double) size;
    }

    public synchronized double jitter() {
        return jitter;
    }

    public synchronized int suspectedLoss() {
        return suspectedLoss;
    }

    /**
     * Extra tolerance multiplier for movement checks based on recent connection quality.
     * 1.0 = normal, higher = more lenient. Never disables checks by itself.
     */
    public synchronized double movementToleranceMultiplier() {
        double mult = 1.0;
        if (lastPing > 100) {
            mult += Math.min(0.5, (lastPing - 100) / 400.0);
        }
        if (jitter > 30) {
            mult += Math.min(0.3, (jitter - 30) / 200.0);
        }
        if (suspectedLoss > 2) {
            mult += Math.min(0.4, suspectedLoss * 0.05);
        }
        return mult;
    }

    public synchronized boolean isConnectionUnstable() {
        return jitter > 80 || suspectedLoss > 5 || lastPing > 400;
    }

    private double computeJitter() {
        if (size < 2) {
            return 0;
        }
        double mean = averagePing();
        double acc = 0;
        for (int i = 0; i < size; i++) {
            double d = pingSamples[i] - mean;
            acc += d * d;
        }
        return Math.sqrt(acc / size);
    }
}
