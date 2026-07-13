# Changelog

## 2.0.0-SNAPSHOT — Modern core + full check suite foundation

### Added
- Gradle multi-module (Java 21): api, common, spigot, proxy-common, bungee, velocity, tests
- Public API + Bukkit events: Flag / Punish / Setback
- Managers: PlayerData, Violation (buffer/decay), Exemption, Latency, CheckManager, PacketCounters
- Storage async: SQLite + file logs
- Packet layer: ProtocolLib when present, Bukkit approximation otherwise
- Proxy alert protocol (v2) with server-sender validation + Bungee/Velocity fan-out
- Shared flag pipeline (`CheckSupport`) with storage, alerts, setback, punishments
- Unit tests: buffer, exemption, latency, version, AABB, proxy codec

### Checks (modern module)
Movement: Speed, Fly, Timer, NoFall, Jesus, Spider, Step, Phase, NoSlow, AirJump, NoWeb, FastLadder, ElytraFly, InventoryMove  
Combat: Reach, KillAura, AutoClick, Criticals, Velocity, FastBow  
World/Player: Scaffold, Nuker, FastPlace, Regen  
Packet: BadPackets (A–F)

### Fixed (legacy `/src`)
- TimerAnalyzePacket `return` → `continue`
- Hard-disable modern versions softened
- Paper remapped version detection
- Concurrent player/packet maps

### Notes
- Punishments default **disabled** until tuned
- Legacy V1 sources remain under `/src` for reference
- Some advanced cheats (AimAssist ML, full Blink transaction, MySQL pool) still iterative
