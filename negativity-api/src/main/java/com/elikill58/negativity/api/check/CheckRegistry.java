package com.elikill58.negativity.api.check;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

public interface CheckRegistry {

    @NotNull
    Collection<CheckInfo> all();

    @NotNull
    Optional<CheckInfo> find(@NotNull String id);

    boolean isEnabled(@NotNull String id);

    /**
     * Enable or disable a check at runtime (persists if configuration allows).
     */
    void setEnabled(@NotNull String id, boolean enabled);
}
