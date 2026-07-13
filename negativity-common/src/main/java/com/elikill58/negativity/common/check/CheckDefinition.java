package com.elikill58.negativity.common.check;

import com.elikill58.negativity.api.check.CheckCategory;
import com.elikill58.negativity.common.violation.CheckThresholds;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Internal check registration metadata + thresholds.
 */
public final class CheckDefinition {

    private final String id;
    private final String displayName;
    private final CheckCategory category;
    private volatile boolean enabled;
    private volatile CheckThresholds thresholds;

    public CheckDefinition(
            @NotNull String id,
            @NotNull String displayName,
            @NotNull CheckCategory category,
            boolean enabled,
            @NotNull CheckThresholds thresholds
    ) {
        this.id = Objects.requireNonNull(id).toLowerCase();
        this.displayName = Objects.requireNonNull(displayName);
        this.category = Objects.requireNonNull(category);
        this.enabled = enabled;
        this.thresholds = Objects.requireNonNull(thresholds);
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public CheckCategory category() {
        return category;
    }

    public boolean enabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public CheckThresholds thresholds() {
        return thresholds;
    }

    public void setThresholds(@NotNull CheckThresholds thresholds) {
        this.thresholds = Objects.requireNonNull(thresholds);
    }
}
