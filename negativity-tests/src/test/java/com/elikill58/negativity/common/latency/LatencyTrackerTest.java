package com.elikill58.negativity.common.latency;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LatencyTrackerTest {

    @Test
    void averageAndTolerance() {
        LatencyTracker tracker = new LatencyTracker(5);
        tracker.recordPing(50);
        tracker.recordPing(60);
        tracker.recordPing(55);
        assertEquals(55.0, tracker.averagePing(), 0.01);
        assertEquals(1.0, tracker.movementToleranceMultiplier(), 0.01);
    }

    @Test
    void highPingIncreasesTolerance() {
        LatencyTracker tracker = new LatencyTracker(5);
        tracker.recordPing(250);
        assertTrue(tracker.movementToleranceMultiplier() > 1.0);
    }
}
