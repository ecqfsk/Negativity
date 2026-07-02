# Platform availability

## Why a detection is not available

A check runs only where the event it listens to is fired. Below is every check that is
**not** available on at least one platform, with the missing event as the reason. Checks not
listed here are available on all detection platforms (Spigot, Sponge 8, Sponge 7, Minestom,
Fabric): `AutoArmor/fast-swap`, `AutoSteal/time-click`, `FastBow/last-shot`,
`IllegalItems/illegal`, `NoSlowDown/eat`, `Regen/time`.

**Bungee / Velocity are proxies: they run no detections at all**, so every check below (and the
ones above) is inapplicable there — not repeated per row.

| Cheat            | Check              | Platform                       | Missing event                  |
| ---------------- | ------------------ | ------------------------------ | ------------------------------ |
| AimBot           | ratio              | Fabric                         | PacketReceiveEvent             |
| AimBot           | gcd                | Fabric                         | PacketReceiveEvent             |
| AimBot           | direction          | Fabric                         | PlayerDamageEntityEvent        |
| AirJump          | diff-y             | Fabric                         | PlayerMoveEvent                |
| AirJump          | going-down         | Fabric                         | PlayerMoveEvent                |
| AirPlace         | block-around       | Fabric                         | BlockPlaceEvent                |
| AntiKnockback    | packet             | Fabric                         | PacketSendEvent                |
| AutoClick        | count              | Fabric                         | PacketReceiveEvent             |
| AutoTotem        | instant-refill     | Sponge 8, Sponge 7, Minestom   | EntityResurrectionEvent        |
| Bhop             | chained-jump       | Fabric                         | PlayerMoveEvent                |
| BoatFly          | boat-ascend        | Fabric                         | PlayerMoveEvent                |
| Chat             | spam               | Fabric                         | PlayerChatEvent                |
| Chat             | insult             | Fabric                         | PlayerChatEvent                |
| Chat             | caps               | Fabric                         | PlayerChatEvent                |
| CommandSpam      | rate               | Sponge 8, Fabric               | PlayerCommandPreProcessEvent   |
| Critical         | ground             | Fabric                         | PlayerDamageEntityEvent        |
| Critical         | y-pos              | Fabric                         | PlayerDamageEntityEvent        |
| ElytraFly        | diff-y             | Fabric                         | PlayerMoveEvent                |
| FastLadder       | distance           | Fabric                         | PlayerMoveEvent                |
| FastPlace        | time               | Fabric                         | PacketReceiveEvent             |
| FastStairs       | distance           | Fabric                         | PlayerMoveEvent                |
| Fly              | no-ground-down     | Fabric                         | PlayerMoveEvent                |
| Fly              | omega-craft        | Fabric                         | PlayerMoveEvent                |
| Fly              | ground-checker     | Fabric                         | PacketReceiveEvent             |
| Fly              | suspicious-y       | Fabric                         | PlayerMoveEvent                |
| Fly              | no-ground-y        | Fabric                         | PlayerMoveEvent                |
| Fly              | not-moving-y       | Fabric                         | PacketReceiveEvent             |
| Fly              | no-ground-i        | Fabric                         | PlayerMoveEvent                |
| ForceField       | packet             | Fabric                         | PlayerPacketsClearEvent        |
| ForceField       | line-sight         | Fabric                         | PlayerDamageEntityEvent        |
| GroundSpoof      | check-blocks-under | Fabric                         | PlayerMoveEvent                |
| IncorrectPacket  | distance           | Fabric                         | PacketPreReceiveEvent          |
| InventoryMove    | stay-distance      | Fabric                         | PlayerMoveEvent                |
| Jesus            | ground-water       | Fabric                         | PlayerMoveEvent                |
| Jesus            | dif-y-2-move       | Fabric                         | PlayerMoveEvent                |
| Jesus            | dif                | Fabric                         | PlayerMoveEvent                |
| Jesus            | water-around       | Fabric                         | PlayerMoveEvent                |
| Jesus            | distance-in        | Fabric                         | PlayerMoveEvent                |
| Motion           | y-motion           | Fabric                         | PacketReceiveEvent             |
| NoFall           | motion-y           | Fabric                         | PlayerMoveEvent                |
| NoFall           | distance-no-ground | Fabric                         | PlayerMoveEvent                |
| NoFall           | distance-ground    | Fabric                         | PlayerMoveEvent                |
| NoFall           | have-to-ground     | Fabric                         | PlayerMoveEvent                |
| NoFall           | packet             | Fabric                         | PacketReceiveEvent             |
| NoFall           | fake-ground        | Fabric                         | PlayerMoveEvent                |
| NoPitchLimit     | head-mov           | Fabric                         | PlayerMoveEvent                |
| NoSlowDown       | move               | Fabric                         | PlayerMoveEvent                |
| NoWeb            | speed              | Fabric                         | PlayerMoveEvent                |
| Nuker            | time               | Fabric                         | BlockBreakEvent                |
| Nuker            | packet             | Fabric                         | PlayerPacketsClearEvent        |
| PingSpoof        | packet             | Fabric                         | PacketEvent (packets)          |
| Reach            | reach-event        | Fabric                         | PacketReceiveEvent             |
| Scaffold         | below              | Fabric                         | BlockPlaceEvent                |
| Scaffold         | distance           | Fabric                         | BlockPlaceEvent                |
| Scaffold         | packet             | Fabric                         | PacketPreReceiveEvent          |
| Scaffold         | place-below        | Fabric                         | BlockPlaceEvent                |
| Scaffold         | rise-slot          | Fabric                         | PacketReceiveEvent             |
| Sneak            | sneak-sprint       | Fabric                         | PlayerMoveEvent                |
| Sneak            | packet             | Fabric                         | PlayerPacketsClearEvent        |
| Speed            | distance-jumping   | Fabric                         | PlayerMoveEvent                |
| Speed            | same-diff          | Fabric                         | PlayerMoveEvent                |
| Speed            | walk-speed         | Fabric                         | PacketReceiveEvent             |
| Speed            | high-speed         | Fabric                         | PlayerMoveEvent                |
| Speed            | distance-vehicle   | Fabric                         | PacketPreReceiveEvent          |
| Spider           | nothing-around     | Fabric                         | PlayerMoveEvent                |
| Spider           | same-y             | Fabric                         | PlayerMoveEvent                |
| Spider           | distance           | Fabric                         | PlayerMoveEvent                |
| Step             | dif                | Fabric                         | PlayerMoveEvent                |
| Step             | dif-boost          | Fabric                         | PlayerMoveEvent                |
| Step             | dif-no-xz          | Fabric                         | PlayerMoveEvent                |
| Strafe           | direction          | Fabric                         | PlayerMoveEvent                |
| SuperKnockback   | diff               | Fabric                         | PacketReceiveEvent             |
| Timer            | packet             | Fabric                         | PlayerPacketsClearEvent        |
| UnexpectedPacket | vehicle-steer      | Fabric                         | PacketReceiveEvent             |
| UnexpectedPacket | spectator          | Fabric                         | PacketReceiveEvent             |
| UnexpectedPacket | held-change        | Fabric                         | PacketReceiveEvent             |
| WTap             | sprint-toggle      | Fabric                         | PlayerMoveEvent                |
| XRay             | minerate           | Fabric                         | BlockBreakEvent                |
| XRay             | mining-direction   | Fabric                         | BlockBreakEvent                |

On **Fabric**, only checks driven by `InventoryClickEvent`, `PlayerInteractEvent`,
`PlayerItemConsumeEvent`, `PlayerRegainHealthEvent` and `EntityResurrectionEvent` run — the whole
packet pipeline (`PlayerMoveEvent`, `Packet*`, `BlockBreak/BlockPlace`, `PlayerChat`,
`PlayerDamageEntity`, `PlayerPacketsClear`, `PlayerCommandPreProcess`) is not wired there.


## Full event availability — events NOT fired per platform

Here are list of events not fired, per platform, that should be fired in the future. (Currently not fired because not existing/not added)

### Spigot
- none

### Sponge 8 (API 8)
- `EntityResurrectionEvent`
- `EntityShootBowEvent`
- `PlayerCommandPreProcessEvent`
- `PlayerDamagedByEntityEvent` (uses `PlayerDamageEntityEvent` instead)
- `ProjectileHitEvent`

### Sponge 7 (API 7)
- `EntityResurrectionEvent`
- `EntityShootBowEvent`
- `ProjectileHitEvent`

### Minestom
- `EntityDismountEvent`
- `EntityResurrectionEvent`
- `PlayerChangeWorldEvent`
- `GameChannelNegativityMessageEvent`

### Fabric (repo `NegativityFabric`, MC ≥ 1.20 modules)
- All packet-driven events: `PlayerMoveEvent`, `PlayerChatEvent`, `PlayerToggleActionEvent`,
  `PlayerDamageEntityEvent`, `BlockBreakEvent`, `PacketReceiveEvent`, `PacketPreReceiveEvent`,
  `PacketSendEvent`
- `BlockPlaceEvent`
- `EntityDismountEvent`
- `EntityShootBowEvent`
- `InventoryOpenEvent`
- `PlayerChangeWorldEvent`
- `PlayerCommandPreProcessEvent`
- `ProjectileHitEvent`
- `GameChannelNegativityMessageEvent`, `ProxyChannelNegativityMessageEvent`
- (`EntityResurrectionEvent` IS available, ≥ 1.20 modules only)

### Bungee / Velocity (proxies)
- none
