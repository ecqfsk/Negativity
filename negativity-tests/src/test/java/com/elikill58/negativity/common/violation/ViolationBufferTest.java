package com.elikill58.negativity.common.violation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ViolationBufferTest {

    @Test
    void addIncreasesBuffer() {
        ViolationBuffer buffer = new ViolationBuffer(0.0, 50);
        assertEquals(3.0, buffer.add(3.0), 0.0001);
        assertEquals(5.0, buffer.add(2.0), 0.0001);
    }

    @Test
    void respectsMaxValue() {
        ViolationBuffer buffer = new ViolationBuffer(0.0, 10);
        buffer.add(100);
        assertEquals(10.0, buffer.get(), 0.0001);
    }

    @Test
    void decayReducesOverTime() throws InterruptedException {
        ViolationBuffer buffer = new ViolationBuffer(10.0, 50); // 10 per second
        buffer.add(10);
        Thread.sleep(200); // ~2 decay
        double value = buffer.get();
        assertTrue(value < 10.0, "expected decay, got " + value);
        assertTrue(value > 5.0, "should not decay too fast in 200ms, got " + value);
    }

    @Test
    void resetClears() {
        ViolationBuffer buffer = new ViolationBuffer(1.0, 50);
        buffer.add(8);
        buffer.reset();
        assertEquals(0.0, buffer.get(), 0.0001);
    }
}
