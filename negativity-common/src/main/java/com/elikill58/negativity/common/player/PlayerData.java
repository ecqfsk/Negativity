package com.elikill58.negativity.common.player;

import com.elikill58.negativity.api.player.PlayerSnapshot;
import com.elikill58.negativity.common.latency.LatencyTracker;
import com.elikill58.negativity.common.packet.PacketCounters;
import com.elikill58.negativity.common.violation.ViolationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Per-player runtime state. Keep this lean — only data needed by detections.
 */
public final class PlayerData implements PlayerSnapshot {

    private final UUID uuid;
    private volatile String name;
    private final LatencyTracker latency = new LatencyTracker(20);
    private final PacketCounters packets = new PacketCounters();
    private final AtomicBoolean analyzed = new AtomicBoolean(true);
    private volatile long lastAttackMs;
    private volatile long lastVelocityMs;
    private volatile int cpsWindow;
    private volatile long cpsWindowStartMs = System.currentTimeMillis();
    private volatile boolean bedrock;
    private volatile @Nullable String clientBrand;
    private volatile @Nullable String worldName;

    // last known safe position for setbacks (world + xyz + yaw/pitch)
    private volatile @Nullable SafePosition lastSafePosition;

    // join / combat / movement helpers
    private volatile long joinedAtMs = System.currentTimeMillis();
    private volatile long lastTeleportMs;
    private volatile boolean onGround = true;
    private volatile boolean wasOnGround = true;
    private volatile double lastDeltaX;
    private volatile double lastDeltaY;
    private volatile double lastDeltaZ;
    private volatile float lastYaw;
    private volatile float lastPitch;

    // generic check-private bags (typed modules should prefer dedicated fields)
    private final Map<String, Object> checkData = new ConcurrentHashMap<>();

    private final ViolationManager violationManager;

    public PlayerData(@NotNull UUID uuid, @NotNull String name, @NotNull ViolationManager violationManager) {
        this.uuid = Objects.requireNonNull(uuid);
        this.name = Objects.requireNonNull(name);
        this.violationManager = Objects.requireNonNull(violationManager);
    }

    @Override
    public @NotNull UUID uuid() {
        return uuid;
    }

    @Override
    public @NotNull String name() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = Objects.requireNonNull(name);
    }

    @Override
    public int ping() {
        return latency.ping();
    }

    @Override
    public double jitter() {
        return latency.jitter();
    }

    @NotNull
    public LatencyTracker latency() {
        return latency;
    }

    @NotNull
    public PacketCounters packets() {
        return packets;
    }

    public long lastAttackMs() {
        return lastAttackMs;
    }

    public void markAttack() {
        this.lastAttackMs = System.currentTimeMillis();
    }

    public long lastVelocityMs() {
        return lastVelocityMs;
    }

    public void markVelocity() {
        this.lastVelocityMs = System.currentTimeMillis();
    }

    public int recordClick() {
        long now = System.currentTimeMillis();
        if (now - cpsWindowStartMs >= 1000L) {
            cpsWindow = 0;
            cpsWindowStartMs = now;
        }
        return ++cpsWindow;
    }

    public int currentCps() {
        long now = System.currentTimeMillis();
        if (now - cpsWindowStartMs >= 1000L) {
            return 0;
        }
        return cpsWindow;
    }

    @Override
    public boolean isAnalyzed() {
        return analyzed.get();
    }

    public void setAnalyzed(boolean value) {
        analyzed.set(value);
    }

    @Override
    public boolean isBedrock() {
        return bedrock;
    }

    public void setBedrock(boolean bedrock) {
        this.bedrock = bedrock;
    }

    @Override
    public @Nullable String clientBrand() {
        return clientBrand;
    }

    public void setClientBrand(@Nullable String clientBrand) {
        this.clientBrand = clientBrand;
    }

    @Override
    public @Nullable String worldName() {
        return worldName;
    }

    public void setWorldName(@Nullable String worldName) {
        this.worldName = worldName;
    }

    @Override
    public double violationLevel(@NotNull String checkId) {
        return violationManager.getVl(uuid, checkId);
    }

    public long joinedAtMs() {
        return joinedAtMs;
    }

    public long lastTeleportMs() {
        return lastTeleportMs;
    }

    public void markTeleport() {
        this.lastTeleportMs = System.currentTimeMillis();
    }

    public boolean isOnGround() {
        return onGround;
    }

    public void setOnGround(boolean onGround) {
        this.wasOnGround = this.onGround;
        this.onGround = onGround;
    }

    public boolean wasOnGround() {
        return wasOnGround;
    }

    public void setLastDelta(double dx, double dy, double dz) {
        this.lastDeltaX = dx;
        this.lastDeltaY = dy;
        this.lastDeltaZ = dz;
    }

    public double lastDeltaX() {
        return lastDeltaX;
    }

    public double lastDeltaY() {
        return lastDeltaY;
    }

    public double lastDeltaZ() {
        return lastDeltaZ;
    }

    public float lastYaw() {
        return lastYaw;
    }

    public float lastPitch() {
        return lastPitch;
    }

    public void setLastRotation(float yaw, float pitch) {
        this.lastYaw = yaw;
        this.lastPitch = pitch;
    }

    public @Nullable SafePosition lastSafePosition() {
        return lastSafePosition;
    }

    public void setLastSafePosition(@Nullable SafePosition lastSafePosition) {
        this.lastSafePosition = lastSafePosition;
    }

    @SuppressWarnings("unchecked")
    public <T> T getCheckData(@NotNull String key, @NotNull T defaultValue) {
        Object value = checkData.get(key);
        if (value == null) {
            return defaultValue;
        }
        return (T) value;
    }

    public void setCheckData(@NotNull String key, @Nullable Object value) {
        if (value == null) {
            checkData.remove(key);
        } else {
            checkData.put(key, value);
        }
    }

    public record SafePosition(
            @NotNull String world,
            double x,
            double y,
            double z,
            float yaw,
            float pitch
    ) {
    }
}
