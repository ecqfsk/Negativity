package com.elikill58.negativity.api;

import com.elikill58.negativity.api.check.CheckRegistry;
import com.elikill58.negativity.api.player.PlayerDataService;
import com.elikill58.negativity.api.violation.ViolationService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

/**
 * Public entry point for third-party plugins.
 * Internal classes are never exposed directly through this API.
 */
public final class NegativityAPI {

    private static volatile NegativityAPI instance;

    private final PlayerDataService players;
    private final ViolationService violations;
    private final CheckRegistry checks;
    private final ExemptionService exemptions;

    public NegativityAPI(
            @NotNull PlayerDataService players,
            @NotNull ViolationService violations,
            @NotNull CheckRegistry checks,
            @NotNull ExemptionService exemptions
    ) {
        this.players = Objects.requireNonNull(players, "players");
        this.violations = Objects.requireNonNull(violations, "violations");
        this.checks = Objects.requireNonNull(checks, "checks");
        this.exemptions = Objects.requireNonNull(exemptions, "exemptions");
    }

    public static void setInstance(@Nullable NegativityAPI api) {
        instance = api;
    }

    @NotNull
    public static NegativityAPI get() {
        NegativityAPI api = instance;
        if (api == null) {
            throw new IllegalStateException("Negativity is not loaded yet.");
        }
        return api;
    }

    public static boolean isAvailable() {
        return instance != null;
    }

    @NotNull
    public PlayerDataService players() {
        return players;
    }

    @NotNull
    public ViolationService violations() {
        return violations;
    }

    @NotNull
    public CheckRegistry checks() {
        return checks;
    }

    @NotNull
    public ExemptionService exemptions() {
        return exemptions;
    }

    public boolean isBeingAnalyzed(@NotNull UUID uuid) {
        return players.get(uuid).map(p -> p.isAnalyzed()).orElse(false);
    }
}
