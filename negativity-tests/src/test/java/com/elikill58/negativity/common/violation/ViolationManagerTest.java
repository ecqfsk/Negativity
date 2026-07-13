package com.elikill58.negativity.common.violation;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ViolationManagerTest {

    @Test
    void progressiveAlertThreshold() {
        ViolationManager manager = new ViolationManager(32);
        UUID id = UUID.randomUUID();
        CheckThresholds thresholds = new CheckThresholds(5, 10, 25, 0, 50, 0, 1.0, false, true, false);

        ViolationManager.FlagResult first = manager.flag(new ViolationManager.FlagRequest(
                id, "Steve", "speed", "A", 2.0, 40, 20.0, "test", "d1", thresholds
        ));
        assertFalse(first.shouldAlert());

        ViolationManager.FlagResult second = manager.flag(new ViolationManager.FlagRequest(
                id, "Steve", "speed", "A", 4.0, 40, 20.0, "test", "d2", thresholds
        ));
        assertTrue(second.shouldAlert());
        assertEquals(6.0, second.vl(), 0.001);
    }

    @Test
    void resetPlayer() {
        ViolationManager manager = new ViolationManager(16);
        UUID id = UUID.randomUUID();
        CheckThresholds thresholds = CheckThresholds.defaults();
        manager.flag(new ViolationManager.FlagRequest(
                id, "Alex", "fly", "A", 3.0, 20, 20.0, null, null, thresholds
        ));
        manager.reset(id);
        assertEquals(0.0, manager.getVl(id, "fly"), 0.0001);
    }
}
