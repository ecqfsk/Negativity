package com.elikill58.negativity.common.version;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinecraftVersionTest {

    @Test
    void parsePaperVersions() {
        assertEquals(MinecraftVersion.V1_21_4, MinecraftVersion.fromBukkitVersion("1.21.4-R0.1-SNAPSHOT"));
        assertEquals(MinecraftVersion.V1_20_5, MinecraftVersion.fromBukkitVersion("1.20.6-R0.1-SNAPSHOT"));
        assertEquals(MinecraftVersion.V1_8, MinecraftVersion.fromBukkitVersion("1.8.8-R0.1-SNAPSHOT"));
    }

    @Test
    void modernPaperFlag() {
        assertTrue(MinecraftVersion.V1_21.supportsModernPaper());
        assertTrue(MinecraftVersion.V1_20_5.supportsModernPaper());
    }
}
