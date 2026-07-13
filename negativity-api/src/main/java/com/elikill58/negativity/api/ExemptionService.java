package com.elikill58.negativity.api;

import com.elikill58.negativity.api.exempt.ExemptReason;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;

/**
 * Centralized exemption API for Negativity and third-party plugins.
 */
public interface ExemptionService {

    /**
     * Exempts a player from all checks for the given duration.
     */
    void exempt(@NotNull UUID playerId, @NotNull ExemptReason reason, long durationMs);

    /**
     * Exempts a player from a specific check (or family) for the given duration.
     *
     * @param checkId check id such as {@code speed}, {@code fly}, or {@code *} for all
     */
    void exempt(@NotNull UUID playerId, @NotNull String checkId, @NotNull ExemptReason reason, long durationMs);

    /**
     * Removes all exemptions of a given reason for the player.
     */
    void clear(@NotNull UUID playerId, @NotNull ExemptReason reason);

    /**
     * Removes every exemption for the player.
     */
    void clearAll(@NotNull UUID playerId);

    /**
     * @return true if the player is currently exempt from the check
     */
    boolean isExempt(@NotNull UUID playerId, @NotNull String checkId);

    /**
     * @return human-readable reasons why analysis is skipped (empty if not exempt)
     */
    @NotNull
    Collection<String> debugExemptions(@NotNull UUID playerId, @NotNull String checkId);
}
