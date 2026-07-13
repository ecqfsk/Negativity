package com.elikill58.negativity.common.config;

/**
 * Default configuration document (YAML) used on first run and for key migration.
 */
public final class ConfigDefaults {

    private ConfigDefaults() {
    }

    public static String configYml() {
        return """
                # Negativity modernized configuration
                # Safe defaults — tune after observing false positives.

                debug: false
                language: pt_BR

                server-name: "server"

                performance:
                  # Global tick task interval in ticks (20 = 1 second)
                  tick-interval: 20

                latency:
                  # Soft thresholds — checks use multipliers, they are not hard-disabled
                  soft-ping-ms: 200
                  unstable-ping-ms: 400
                  history-size: 20

                tps:
                  # Soft lag multiplier starts below this TPS
                  soft-threshold: 19.0
                  # Extreme lag: widen tolerances significantly
                  hard-threshold: 15.0

                exemptions:
                  join-ms: 8000
                  teleport-ms: 2000
                  respawn-ms: 2000
                  world-change-ms: 3000
                  knockback-ms: 750

                alerts:
                  enabled: true
                  cooldown-ms: 1000
                  clickable: true
                  permission: "negativity.alert"
                  format: "&c[Negativity] &e%player% &7falhou &c%check% %subcheck% &7| VL: &f%vl% &7| Buffer: &f%buffer% &7| Ping: &f%ping%ms &7| TPS: &f%tps%"

                storage:
                  type: sqlite # file | sqlite | mysql (mysql later)
                  async: true
                  sqlite-file: "violations.db"

                proxy:
                  enabled: true
                  channel: "negativity:msg"
                  # Only accept plugin messages from backend servers, never from players
                  require-server-sender: true
                """;
    }

    public static String checksYml() {
        return """
                # Per-check configuration
                checks:
                  speed:
                    enabled: true
                    exact_name: "Speed"
                    alert-vl: 5.0
                    setback-vl: 10.0
                    punish-vl: 25.0
                    buffer-decay: 0.6
                    buffer-max: 40.0
                    alert-cooldown-ms: 1000
                    weight: 1.0
                    setback: true
                    cancel: false
                    log-only: false
                    # horizontal distance thresholds (blocks/tick) before latency multipliers
                    ground-max: 0.36
                    air-max: 0.42
                    commands: []

                  timer:
                    enabled: true
                    exact_name: "Timer"
                    alert-vl: 6.0
                    setback-vl: 12.0
                    punish-vl: 30.0
                    buffer-decay: 0.4
                    buffer-max: 50.0
                    alert-cooldown-ms: 1500
                    weight: 1.0
                    setback: false
                    cancel: false
                    log-only: false
                    max-packets-per-second: 22
                    sample-seconds: 5
                    commands: []

                  fly:
                    enabled: true
                    exact_name: "Fly"
                    alert-vl: 5.0
                    setback-vl: 10.0
                    punish-vl: 25.0
                    buffer-decay: 0.5
                    buffer-max: 40.0
                    alert-cooldown-ms: 1000
                    weight: 1.0
                    setback: true
                    cancel: false
                    log-only: false
                    air-ticks-threshold: 12
                    commands: []
                """;
    }

    public static String messagesYml() {
        return """
                prefix: "&c[Negativity] &r"
                no-permission: "%prefix%&cVocê não tem permissão."
                player-not-found: "%prefix%&cJogador não encontrado."
                reload-done: "%prefix%&aConfiguração recarregada (componentes seguros)."
                reload-partial: "%prefix%&eReload parcial: listeners de pacote não foram reiniciados."
                alerts-enabled: "%prefix%&aAlertas ativados."
                alerts-disabled: "%prefix%&cAlertas desativados."
                check-info: "%prefix%&e%player% &7| check &e%check% &7| VL &f%vl% &7| buffer &f%buffer% &7| ping &f%ping%ms"
                exempt-added: "%prefix%&aIsenção aplicada a &e%player% &7(&f%reason%&7)."
                debug-header: "%prefix%&7Debug &e%player% &7/ &e%check%&7:"
                """;
    }

    public static String punishmentsYml() {
        return """
                # Progressive punishments (executed on main thread via platform adapter)
                enabled: true
                # Global commands when punish-vl is reached if check has no specific commands
                default-commands:
                  - "kick %player% Negativity: %check% (VL %vl%)"
                placeholders:
                  # %player% %uuid% %check% %subcheck% %vl% %ping% %tps% %server% %buffer%
                """;
    }
}
