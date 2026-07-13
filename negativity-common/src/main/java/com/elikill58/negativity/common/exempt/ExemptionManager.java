package com.elikill58.negativity.common.exempt;

import com.elikill58.negativity.api.ExemptionService;
import com.elikill58.negativity.api.exempt.ExemptReason;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe centralized exemptions with reason + duration.
 */
public final class ExemptionManager implements ExemptionService {

    private static final String ALL = "*";

    public record Exemption(
            @NotNull String checkId,
            @NotNull ExemptReason reason,
            long expiresAtMs,
            @NotNull String detail
    ) {
        public boolean isExpired(long now) {
            return expiresAtMs > 0 && now >= expiresAtMs;
        }

        public boolean matches(@NotNull String targetCheck) {
            return ALL.equals(checkId) || checkId.equalsIgnoreCase(targetCheck);
        }
    }

    private final Map<UUID, List<Exemption>> exemptions = new ConcurrentHashMap<>();

    @Override
    public void exempt(@NotNull UUID playerId, @NotNull ExemptReason reason, long durationMs) {
        exempt(playerId, ALL, reason, durationMs, reason.id());
    }

    /**
     * Global exemption (all checks) with a debug detail string.
     */
    public void exempt(
            @NotNull UUID playerId,
            @NotNull ExemptReason reason,
            long durationMs,
            @NotNull String detail
    ) {
        exempt(playerId, ALL, reason, durationMs, detail);
    }

    @Override
    public void exempt(@NotNull UUID playerId, @NotNull String checkId, @NotNull ExemptReason reason, long durationMs) {
        exempt(playerId, checkId, reason, durationMs, reason.id());
    }

    public void exempt(
            @NotNull UUID playerId,
            @NotNull String checkId,
            @NotNull ExemptReason reason,
            long durationMs,
            @NotNull String detail
    ) {
        Objects.requireNonNull(playerId);
        Objects.requireNonNull(checkId);
        Objects.requireNonNull(reason);
        long expires = durationMs <= 0 ? 0 : System.currentTimeMillis() + durationMs;
        Exemption entry = new Exemption(checkId.toLowerCase(), reason, expires, detail == null ? reason.id() : detail);
        exemptions.compute(playerId, (id, list) -> {
            List<Exemption> result = list == null ? new ArrayList<>() : new ArrayList<>(list);
            // replace same reason+check to avoid unbounded growth
            result.removeIf(e -> e.reason() == reason && e.checkId().equalsIgnoreCase(entry.checkId()));
            result.add(entry);
            return result;
        });
    }

    @Override
    public void clear(@NotNull UUID playerId, @NotNull ExemptReason reason) {
        exemptions.computeIfPresent(playerId, (id, list) -> {
            List<Exemption> next = new ArrayList<>();
            for (Exemption e : list) {
                if (e.reason() != reason) {
                    next.add(e);
                }
            }
            return next.isEmpty() ? null : next;
        });
    }

    @Override
    public void clearAll(@NotNull UUID playerId) {
        exemptions.remove(playerId);
    }

    @Override
    public boolean isExempt(@NotNull UUID playerId, @NotNull String checkId) {
        return !debugExemptions(playerId, checkId).isEmpty();
    }

    @Override
    public @NotNull Collection<String> debugExemptions(@NotNull UUID playerId, @NotNull String checkId) {
        List<Exemption> list = exemptions.get(playerId);
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        long now = System.currentTimeMillis();
        List<String> reasons = new ArrayList<>();
        List<Exemption> kept = new ArrayList<>();
        for (Exemption e : list) {
            if (e.isExpired(now)) {
                continue;
            }
            kept.add(e);
            if (e.matches(checkId)) {
                reasons.add(e.reason().id() + (e.detail().isEmpty() ? "" : " (" + e.detail() + ")"));
            }
        }
        if (kept.size() != list.size()) {
            if (kept.isEmpty()) {
                exemptions.remove(playerId);
            } else {
                exemptions.put(playerId, kept);
            }
        }
        return List.copyOf(reasons);
    }

    public void removePlayer(@NotNull UUID playerId) {
        clearAll(playerId);
    }

    /**
     * Periodic cleanup of expired entries.
     */
    public void tickCleanup() {
        long now = System.currentTimeMillis();
        for (Map.Entry<UUID, List<Exemption>> entry : exemptions.entrySet()) {
            List<Exemption> list = entry.getValue();
            if (list == null) {
                continue;
            }
            boolean changed = false;
            List<Exemption> kept = new ArrayList<>(list.size());
            for (Exemption e : list) {
                if (e.isExpired(now)) {
                    changed = true;
                } else {
                    kept.add(e);
                }
            }
            if (changed) {
                if (kept.isEmpty()) {
                    exemptions.remove(entry.getKey());
                } else {
                    exemptions.put(entry.getKey(), kept);
                }
            }
        }
    }
}
