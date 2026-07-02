# Platform availability

## How a check reaches a platform (mechanism)

A `@Check` runs only where the event it listens to is fired. There are two firing paths:

1. **Packet-driven events** are produced in the **common** module by `NegativityPacketInListener`
   from decoded packets: `PlayerMoveEvent`, `PlayerChatEvent`, `PlayerCommandPreProcessEvent`,
   `PlayerToggleActionEvent`, `BlockBreakEvent`, `PlayerDamageEntityEvent`, plus the raw
   `PacketReceiveEvent` / `PacketPreReceiveEvent` / `PacketSendEvent`.
   Packets are fed to it through:
   - the **netty channel pipeline** (`PacketListeners extends NettyPacketListener` → common
     `NettyDecoderHandler` reads the ByteBuf by protocol version) on **Spigot, Sponge 8,
     Sponge 7 and Fabric (MC ≥ 1.20)**;
   - **Minestom's own packet listener** (builds the `NPacket` and fires `PacketReceiveEvent`).

   So these events are wired on **all five detection platforms**, Fabric included — they do **not**
   appear as `new PlayerMoveEvent(...)` in each platform's source, which is why a naive source
   grep wrongly reports them missing.

2. **Platform-native events** are fired directly by each platform's listeners/mixins
   (`InventoryClickEvent`, `PlayerInteractEvent`, `PlayerItemConsumeEvent`,
   `PlayerRegainHealthEvent`, `BlockPlaceEvent`, `EntityResurrectionEvent`, …). Availability
   depends on whether that platform actually wires them.

**Bungee / Velocity are proxies: no gameplay detection runs there**, so every check is
inapplicable — not repeated below.

> History note: Fabric once fired `PlayerMoveEvent` explicitly in `FabricPacketManager`
> (from `NPacketPlayInFlying`). That class was removed in commit `bc917e4`
> ("Fix for snapshot Negativity") when Fabric was migrated to the shared netty pipeline above.
> Movement detection is therefore still wired — through common, not through Fabric source.

## Checks not available

| Cheat     | Check          | Platform                     | Reason                                                             |
| --------- | -------------- | ---------------------------- | ----------------------------------------------------------------- |
| AutoTotem | instant-refill | Sponge 8, Sponge 7, Minestom | `EntityResurrectionEvent` — no native totem/resurrection event    |
| CommandSpam | rate         | Sponge 8, Fabric             | `PlayerCommandPreProcessEvent` not fired (no command listener; on MC ≥ 1.19 the command packet is separate from chat, so it is not derived from `NPacketPlayInChat`) |
| AirPlace  | block-around   | Fabric                       | `BlockPlaceEvent` not fired (Fabric has no block listener, and block-place is not packet-derived) |
| Scaffold  | below          | Fabric                       | `BlockPlaceEvent` not fired                                        |
| Scaffold  | distance       | Fabric                       | `BlockPlaceEvent` not fired                                        |
| Scaffold  | place-below    | Fabric                       | `BlockPlaceEvent` not fired                                        |

### To verify at runtime (chat packet)
`Chat` checks (`spam`, `insult`, `caps`) rely on `PlayerChatEvent`, derived from
`NPacketPlayInChat`. On MC ≥ 1.19 the serverbound chat packet is signed/changed; if the common
decoder does not map it on a given version, `PlayerChatEvent` will not fire there
(**Fabric ≥ 1.20** and **Sponge 8** are the candidates). Confirm on a live server.

All other checks — every movement, packet, block-break, combat (`PlayerDamageEntity`) and
`PlayerPacketsClear` check — are available on all five detection platforms via the packet
pipeline, **including Fabric**.

## Events not available per platform (reference)

Only events that are genuinely not reachable are listed (packet-driven events reachable through
the pipeline are omitted). Events marked "(unused)" are not consumed by any current check.

### Spigot
- none

### Sponge 8 (API 8)
- `PlayerCommandPreProcessEvent`
- `EntityResurrectionEvent`
- `EntityShootBowEvent` (unused), `ProjectileHitEvent` (unused), `PlayerDamagedByEntityEvent` (unused — uses `PlayerDamageEntityEvent`)

### Sponge 7 (API 7)
- `EntityResurrectionEvent`
- `EntityShootBowEvent` (unused), `ProjectileHitEvent` (unused)

### Minestom
- `EntityResurrectionEvent`
- `EntityDismountEvent` (unused), `PlayerChangeWorldEvent` (unused), `GameChannelNegativityMessageEvent` (unused)

### Fabric (repo `NegativityFabric`, MC ≥ 1.20 modules)
- `BlockPlaceEvent`
- `PlayerCommandPreProcessEvent`
- `PlayerChatEvent` — to verify (see chat-packet note)
- `EntityDismountEvent` (unused), `EntityShootBowEvent` (unused), `ProjectileHitEvent` (unused),
  `PlayerChangeWorldEvent` (unused), `InventoryOpenEvent` (unused),
  `GameChannelNegativityMessageEvent` (unused)
- Available: packet-driven events (`PlayerMoveEvent`, `Packet*`, `BlockBreakEvent`,
  `PlayerDamageEntityEvent`, `PlayerToggleActionEvent`, `PlayerPacketsClearEvent`),
  `InventoryClickEvent`, `PlayerInteractEvent`, `PlayerItemConsumeEvent`,
  `PlayerRegainHealthEvent`, and `EntityResurrectionEvent`.

### Bungee / Velocity (proxies)
No gameplay detection runs. Only `LoginEvent`, `PlayerConnectEvent`, `PlayerLeaveEvent`,
`PlayerCommandPreProcessEvent`, `ProxyChannelNegativityMessageEvent` fire.
