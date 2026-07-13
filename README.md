# Negativity (modernized)

Minecraft **anticheat** rebuilt for modern **Paper** (and Purpur-compatible forks), with companions for **BungeeCord** and **Velocity**.

This repository contains:

1. **Modern modules** (recommended) — Java 21, Gradle, buffer-based detections  
2. **Legacy V1 sources** under `/src` — original free plugin (unmaintained upstream)

Upstream project: [Elikill58/Negativity](https://github.com/Elikill58/Negativity)

## Supported targets (modern build)

| Platform | Status |
|----------|--------|
| Paper 1.20.4+ / 1.21.x | Primary |
| Purpur / Paper forks | Compatible via Paper API |
| BungeeCord companion | Minimal (message validation) |
| Velocity companion | Minimal (message validation) |
| Java | **21** (toolchain) |

Legacy 1.8–1.16 is **not** the focus of the modern modules. Keeping full 1.8 support in the same code path would harm modern detections.

## Requirements

- JDK **21**
- Paper (or fork) for the Spigot module
- Optional: ProtocolLib (future packet layer)

## Build

```bash
# Windows
gradlew.bat build

# Linux / macOS
./gradlew build
```

Artifacts:

- `negativity-spigot/build/libs/Negativity-<version>.jar` — put in `plugins/`
- `negativity-bungeecord/build/libs/Negativity-Bungee-<version>.jar`
- `negativity-velocity/build/libs/Negativity-Velocity-<version>.jar`

```bash
./gradlew :negativity-tests:test
```

## Install

1. Build or download the Spigot/Paper JAR  
2. Drop it into the server `plugins/` folder  
3. Start once to generate `config.yml`, `checks.yml`, `messages.yml`, `punishments.yml`  
4. Adjust thresholds; keep `punishments.enabled: false` until tuned  

On proxies, install the matching companion and the plugin on **each backend** where players should be checked.

## Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/negativity` | `negativity.negativity` | Help / root |
| `/negativity alerts` | `negativity.alert` | Toggle personal alerts |
| `/negativity check <player>` | `negativity.check` | Live VL / latency |
| `/negativity violations <player>` | `negativity.negativity` | Recent flags |
| `/negativity debug <player> <check>` | `negativity.admin` | Why analysis is skipped |
| `/negativity reload` | `negativity.reload` | Safe config reload |
| `/negativity info` | — | Version / TPS |
| `/negativity exempt <player> [seconds]` | `negativity.exempt` | Manual exemption |
| `/negativity logs <player>` | `negativity.negativity` | Alias of violations |

Aliases: `/n`, `/neg`

## Configuration

| File | Role |
|------|------|
| `config.yml` | Global, latency, TPS, alerts, storage, proxy |
| `checks.yml` | Per-check enable + VL thresholds + setback |
| `messages.yml` | User-facing messages (pt_BR defaults) |
| `punishments.yml` | Progressive command punishments |

Checks use **buffers**, **decay**, and separate **alert / setback / punish** thresholds. High ping does **not** hard-disable all detections; it increases movement tolerance.

## Public API (for plugins)

```java
NegativityAPI api = NegativityAPI.get();
api.exemptions().exempt(playerId, ExemptReason.PLUGIN, 2000);
api.violations().getVl(playerId, "speed");
api.checks().setEnabled("fly", false);
```

## Architecture (modern)

```
negativity-api          Public interfaces / records
negativity-common       Managers, buffers, version, config defaults
negativity-spigot       Paper plugin + checks + commands
negativity-proxy-common Shared proxy channel/validation
negativity-bungeecord   Bungee companion
negativity-velocity     Velocity companion
negativity-tests        Unit tests
```

## Current checks (modern module)

| Check | Subchecks | Notes |
|-------|-----------|--------|
| Speed | A/B | Ground/air + potions + latency |
| Fly | A/B | Hover / ascend air ticks |
| Timer | A | Movement packets/s window |
| NoFall | A/B | Ground spoof while falling |
| Jesus | A | Walking on liquids |
| Spider | A | Wall climb |
| Step | A | Impossible step height |
| Phase | A | Inside solid blocks |
| NoSlow / NoWeb / FastLadder | A | Slow contexts |
| AirJump | A | Double jump-like |
| ElytraFly | A/B | Glide speed / hover |
| InventoryMove | A | Move with GUI open |
| Reach | A | AABB edge distance |
| KillAura | A/B | Angle + multi-switch |
| AutoClick | A | CPS |
| Criticals / Velocity / FastBow | A | Combat helpers |
| Scaffold / Nuker / FastPlace | A/B | World interaction |
| Regen | A | Regen interval |
| BadPackets | A–F | Spam / impossible rates |

## Known limitations

- ProtocolLib recommended for accurate Timer/BadPackets  
- MySQL connection pool not implemented yet (SQLite/file ready; architecture allows later)  
- Folia not supported  
- Thresholds need live tuning on production traffic  
- Legacy V1 sources under `/src` are not the runtime for modern JAR  

## License / credits

Original Negativity by **Elikill58** and contributors.  
Modernization work is based on that codebase.
