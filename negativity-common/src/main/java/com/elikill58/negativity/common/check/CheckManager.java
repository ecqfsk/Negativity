package com.elikill58.negativity.common.check;

import com.elikill58.negativity.api.check.CheckInfo;
import com.elikill58.negativity.api.check.CheckRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public final class CheckManager implements CheckRegistry {

    private final Map<String, CheckDefinition> checks = new LinkedHashMap<>();

    public void register(@NotNull CheckDefinition definition) {
        Objects.requireNonNull(definition);
        checks.put(definition.id(), definition);
    }

    @NotNull
    public Optional<CheckDefinition> getDefinition(@NotNull String id) {
        return Optional.ofNullable(checks.get(id.toLowerCase()));
    }

    @Override
    public @NotNull Collection<CheckInfo> all() {
        return checks.values().stream()
                .map(this::toInfo)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public @NotNull Optional<CheckInfo> find(@NotNull String id) {
        return getDefinition(id).map(this::toInfo);
    }

    @Override
    public boolean isEnabled(@NotNull String id) {
        CheckDefinition def = checks.get(id.toLowerCase());
        return def != null && def.enabled();
    }

    @Override
    public void setEnabled(@NotNull String id, boolean enabled) {
        CheckDefinition def = checks.get(id.toLowerCase());
        if (def != null) {
            def.setEnabled(enabled);
        }
    }

    private CheckInfo toInfo(CheckDefinition def) {
        return new CheckInfo(
                def.id(),
                def.displayName(),
                def.category(),
                def.enabled(),
                def.thresholds().alertVl(),
                def.thresholds().setbackVl(),
                def.thresholds().punishVl()
        );
    }
}
