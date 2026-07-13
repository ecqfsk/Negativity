package com.elikill58.negativity.api.player;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface PlayerDataService {

    @NotNull
    Optional<PlayerSnapshot> get(@NotNull UUID uuid);

    @NotNull
    Collection<PlayerSnapshot> online();

    boolean isOnline(@NotNull UUID uuid);
}
