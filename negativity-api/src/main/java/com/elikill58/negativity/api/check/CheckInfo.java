package com.elikill58.negativity.api.check;

import org.jetbrains.annotations.NotNull;

/**
 * Read-only metadata about a registered check.
 */
public record CheckInfo(
        @NotNull String id,
        @NotNull String displayName,
        @NotNull CheckCategory category,
        boolean enabled,
        double alertVl,
        double setbackVl,
        double punishVl
) {
}
