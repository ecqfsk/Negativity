package com.elikill58.negativity.common.packet;

import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Per-player packet counters for the current second (reset by tick/timer).
 */
public final class PacketCounters {

    private final Map<PacketKind, AtomicInteger> counts = new EnumMap<>(PacketKind.class);
    private final AtomicInteger total = new AtomicInteger();
    private volatile long windowStartMs = System.currentTimeMillis();

    public void increment(@NotNull PacketKind kind) {
        counts.computeIfAbsent(kind, k -> new AtomicInteger()).incrementAndGet();
        total.incrementAndGet();
    }

    public int get(@NotNull PacketKind kind) {
        AtomicInteger v = counts.get(kind);
        return v == null ? 0 : v.get();
    }

    public int total() {
        return total.get();
    }

    public int movementPackets() {
        return get(PacketKind.FLYING)
                + get(PacketKind.POSITION)
                + get(PacketKind.LOOK)
                + get(PacketKind.POSITION_LOOK);
    }

    public long windowStartMs() {
        return windowStartMs;
    }

    public Map<PacketKind, Integer> snapshotAndReset() {
        Map<PacketKind, Integer> snap = new EnumMap<>(PacketKind.class);
        for (PacketKind kind : PacketKind.values()) {
            AtomicInteger v = counts.get(kind);
            if (v != null) {
                int n = v.getAndSet(0);
                if (n > 0) {
                    snap.put(kind, n);
                }
            }
        }
        total.set(0);
        windowStartMs = System.currentTimeMillis();
        return snap;
    }
}
