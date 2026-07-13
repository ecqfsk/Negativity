package com.elikill58.negativity.common.exempt;

import com.elikill58.negativity.api.exempt.ExemptReason;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExemptionManagerTest {

    @Test
    void globalExemptCoversAllChecks() {
        ExemptionManager manager = new ExemptionManager();
        UUID id = UUID.randomUUID();
        manager.exempt(id, ExemptReason.TELEPORT, 5_000);
        assertTrue(manager.isExempt(id, "speed"));
        assertTrue(manager.isExempt(id, "fly"));
    }

    @Test
    void checkSpecificExempt() {
        ExemptionManager manager = new ExemptionManager();
        UUID id = UUID.randomUUID();
        manager.exempt(id, "speed", ExemptReason.WATER, 5_000);
        assertTrue(manager.isExempt(id, "speed"));
        assertFalse(manager.isExempt(id, "fly"));
    }

    @Test
    void expiredExemptIsIgnored() throws InterruptedException {
        ExemptionManager manager = new ExemptionManager();
        UUID id = UUID.randomUUID();
        manager.exempt(id, ExemptReason.JOIN, 50);
        Thread.sleep(80);
        assertFalse(manager.isExempt(id, "speed"));
    }

    @Test
    void clearByReason() {
        ExemptionManager manager = new ExemptionManager();
        UUID id = UUID.randomUUID();
        manager.exempt(id, ExemptReason.MANUAL, 10_000);
        manager.exempt(id, ExemptReason.TELEPORT, 10_000);
        manager.clear(id, ExemptReason.MANUAL);
        assertFalse(manager.debugExemptions(id, "speed").stream().anyMatch(s -> s.startsWith("manual")));
        assertTrue(manager.isExempt(id, "speed"));
    }
}
