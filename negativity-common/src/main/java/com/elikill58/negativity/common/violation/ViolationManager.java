package com.elikill58.negativity.common.violation;

import com.elikill58.negativity.api.violation.ViolationRecord;
import com.elikill58.negativity.api.violation.ViolationService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;

/**
 * Tracks per-player / per-check violation levels and buffers.
 */
public final class ViolationManager implements ViolationService {

    public enum Action {
        NONE,
        ALERT,
        SETBACK,
        PUNISH
    }

    public record FlagResult(
            double buffer,
            double vl,
            @Nullable Action action,
            boolean shouldAlert,
            boolean shouldSetback,
            boolean shouldPunish,
            boolean shouldCancel
    ) {
    }

    public record FlagRequest(
            @NotNull UUID playerId,
            @NotNull String playerName,
            @NotNull String checkId,
            @NotNull String subcheck,
            double amount,
            int ping,
            double tps,
            @Nullable String server,
            @Nullable String debug,
            @NotNull CheckThresholds thresholds
    ) {
        public FlagRequest {
            Objects.requireNonNull(playerId);
            Objects.requireNonNull(playerName);
            Objects.requireNonNull(checkId);
            Objects.requireNonNull(subcheck);
            Objects.requireNonNull(thresholds);
        }
    }

    private final Map<UUID, Map<String, ViolationBuffer>> buffers = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Double>> violationLevels = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Long>> lastAlertMs = new ConcurrentHashMap<>();
    private final Map<UUID, Deque<ViolationRecord>> history = new ConcurrentHashMap<>();
    private final int historyLimit;
    private final List<Consumer<ViolationRecord>> listeners = Collections.synchronizedList(new ArrayList<>());

    public ViolationManager(int historyLimit) {
        this.historyLimit = Math.max(16, historyLimit);
    }

    public void addListener(@NotNull Consumer<ViolationRecord> listener) {
        listeners.add(Objects.requireNonNull(listener));
    }

    @NotNull
    public FlagResult flag(@NotNull FlagRequest request) {
        CheckThresholds t = request.thresholds();
        ViolationBuffer buffer = buffers
                .computeIfAbsent(request.playerId(), u -> new ConcurrentHashMap<>())
                .computeIfAbsent(request.checkId(), id -> new ViolationBuffer(t.bufferDecayPerSecond(), t.bufferMax()));

        double bufferValue = buffer.add(request.amount() * t.violationWeight());
        double vl = violationLevels
                .computeIfAbsent(request.playerId(), u -> new ConcurrentHashMap<>())
                .merge(request.checkId(), request.amount() * t.violationWeight(), Double::sum);

        Action action = Action.NONE;
        boolean shouldAlert = false;
        boolean shouldSetback = false;
        boolean shouldPunish = false;

        if (!t.logOnly()) {
            if (vl >= t.punishVl()) {
                action = Action.PUNISH;
                shouldPunish = true;
                shouldAlert = true;
                shouldSetback = t.setback();
            } else if (vl >= t.setbackVl() && t.setback()) {
                action = Action.SETBACK;
                shouldSetback = true;
                shouldAlert = true;
            } else if (bufferValue >= t.alertVl() || vl >= t.alertVl()) {
                action = Action.ALERT;
                shouldAlert = true;
            }
        }

        if (shouldAlert && t.alertCooldownMs() > 0) {
            long now = System.currentTimeMillis();
            Long last = lastAlertMs
                    .computeIfAbsent(request.playerId(), u -> new ConcurrentHashMap<>())
                    .get(request.checkId());
            if (last != null && now - last < t.alertCooldownMs()) {
                shouldAlert = false;
                if (action == Action.ALERT) {
                    action = Action.NONE;
                }
            } else if (shouldAlert) {
                lastAlertMs
                        .computeIfAbsent(request.playerId(), u -> new ConcurrentHashMap<>())
                        .put(request.checkId(), now);
            }
        }

        ViolationRecord record = new ViolationRecord(
                request.playerId(),
                request.playerName(),
                request.checkId(),
                request.subcheck(),
                vl,
                bufferValue,
                request.ping(),
                request.tps(),
                request.server(),
                System.currentTimeMillis(),
                request.debug()
        );
        pushHistory(request.playerId(), record);
        for (Consumer<ViolationRecord> listener : listeners) {
            try {
                listener.accept(record);
            } catch (Exception ignored) {
                // listeners must not break detection pipeline
            }
        }

        return new FlagResult(
                bufferValue,
                vl,
                action,
                shouldAlert,
                shouldSetback,
                shouldPunish,
                t.cancel()
        );
    }

    @Override
    public double getVl(@NotNull UUID playerId, @NotNull String checkId) {
        Map<String, Double> map = violationLevels.get(playerId);
        if (map == null) {
            return 0;
        }
        return map.getOrDefault(checkId, 0.0);
    }

    public double getBuffer(@NotNull UUID playerId, @NotNull String checkId) {
        Map<String, ViolationBuffer> map = buffers.get(playerId);
        if (map == null) {
            return 0;
        }
        ViolationBuffer buffer = map.get(checkId);
        return buffer == null ? 0 : buffer.get();
    }

    @Override
    public @NotNull List<ViolationRecord> recent(@NotNull UUID playerId, int limit) {
        Deque<ViolationRecord> deque = history.get(playerId);
        if (deque == null || deque.isEmpty()) {
            return List.of();
        }
        List<ViolationRecord> list = new ArrayList<>(deque);
        Collections.reverse(list);
        if (limit > 0 && list.size() > limit) {
            return List.copyOf(list.subList(0, limit));
        }
        return List.copyOf(list);
    }

    @Override
    public void reset(@NotNull UUID playerId) {
        buffers.remove(playerId);
        violationLevels.remove(playerId);
        lastAlertMs.remove(playerId);
        history.remove(playerId);
    }

    @Override
    public void reset(@NotNull UUID playerId, @NotNull String checkId) {
        Map<String, ViolationBuffer> b = buffers.get(playerId);
        if (b != null) {
            b.remove(checkId);
        }
        Map<String, Double> v = violationLevels.get(playerId);
        if (v != null) {
            v.remove(checkId);
        }
        Map<String, Long> a = lastAlertMs.get(playerId);
        if (a != null) {
            a.remove(checkId);
        }
    }

    public void removePlayer(@NotNull UUID playerId) {
        reset(playerId);
    }

    private void pushHistory(UUID playerId, ViolationRecord record) {
        Deque<ViolationRecord> deque = history.computeIfAbsent(playerId, u -> new ConcurrentLinkedDeque<>());
        deque.addLast(record);
        while (deque.size() > historyLimit) {
            deque.pollFirst();
        }
    }
}
