package com.elikill58.negativity.common.player;

import com.elikill58.negativity.api.player.PlayerDataService;
import com.elikill58.negativity.api.player.PlayerSnapshot;
import com.elikill58.negativity.common.violation.ViolationManager;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerDataManager implements PlayerDataService {

    private final Map<UUID, PlayerData> players = new ConcurrentHashMap<>();
    private final ViolationManager violationManager;

    public PlayerDataManager(@NotNull ViolationManager violationManager) {
        this.violationManager = Objects.requireNonNull(violationManager);
    }

    @NotNull
    public PlayerData getOrCreate(@NotNull UUID uuid, @NotNull String name) {
        return players.compute(uuid, (id, existing) -> {
            if (existing == null) {
                return new PlayerData(id, name, violationManager);
            }
            existing.setName(name);
            return existing;
        });
    }

    @NotNull
    public Optional<PlayerData> getData(@NotNull UUID uuid) {
        return Optional.ofNullable(players.get(uuid));
    }

    public void remove(@NotNull UUID uuid) {
        players.remove(uuid);
        violationManager.removePlayer(uuid);
    }

    @Override
    public @NotNull Optional<PlayerSnapshot> get(@NotNull UUID uuid) {
        return Optional.ofNullable(players.get(uuid));
    }

    @Override
    public @NotNull Collection<PlayerSnapshot> online() {
        return java.util.List.copyOf(players.values());
    }

    @Override
    public boolean isOnline(@NotNull UUID uuid) {
        return players.containsKey(uuid);
    }

    @NotNull
    public Collection<PlayerData> allData() {
        return java.util.List.copyOf(players.values());
    }
}
