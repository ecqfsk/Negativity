package com.elikill58.negativity.spigot.listener;

import com.elikill58.negativity.api.exempt.ExemptReason;
import com.elikill58.negativity.common.packet.PacketKind;
import com.elikill58.negativity.common.player.PlayerData;
import com.elikill58.negativity.spigot.NegativityPlugin;
import com.elikill58.negativity.spigot.check.AirJumpCheck;
import com.elikill58.negativity.spigot.check.ElytraFlyCheck;
import com.elikill58.negativity.spigot.check.FastLadderCheck;
import com.elikill58.negativity.spigot.check.FlyCheck;
import com.elikill58.negativity.spigot.check.InventoryMoveCheck;
import com.elikill58.negativity.spigot.check.JesusCheck;
import com.elikill58.negativity.spigot.check.NoFallCheck;
import com.elikill58.negativity.spigot.check.NoSlowCheck;
import com.elikill58.negativity.spigot.check.NoWebCheck;
import com.elikill58.negativity.spigot.check.PhaseCheck;
import com.elikill58.negativity.spigot.check.SpeedCheck;
import com.elikill58.negativity.spigot.check.SpiderCheck;
import com.elikill58.negativity.spigot.check.StepCheck;
import com.elikill58.negativity.spigot.check.VelocityCheck;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public final class PlayerMoveCheckListener implements Listener {

    private final NegativityPlugin plugin;
    private final SpeedCheck speedCheck;
    private final FlyCheck flyCheck;
    private final NoFallCheck noFallCheck;
    private final JesusCheck jesusCheck;
    private final SpiderCheck spiderCheck;
    private final StepCheck stepCheck;
    private final PhaseCheck phaseCheck;
    private final NoSlowCheck noSlowCheck;
    private final AirJumpCheck airJumpCheck;
    private final InventoryMoveCheck inventoryMoveCheck;
    private final NoWebCheck noWebCheck;
    private final FastLadderCheck fastLadderCheck;
    private final ElytraFlyCheck elytraFlyCheck;
    private final VelocityCheck velocityCheck;

    public PlayerMoveCheckListener(NegativityPlugin plugin) {
        this.plugin = plugin;
        this.speedCheck = new SpeedCheck(plugin);
        this.flyCheck = new FlyCheck(plugin);
        this.noFallCheck = new NoFallCheck(plugin);
        this.jesusCheck = new JesusCheck(plugin);
        this.spiderCheck = new SpiderCheck(plugin);
        this.stepCheck = new StepCheck(plugin);
        this.phaseCheck = new PhaseCheck(plugin);
        this.noSlowCheck = new NoSlowCheck(plugin);
        this.airJumpCheck = new AirJumpCheck(plugin);
        this.inventoryMoveCheck = new InventoryMoveCheck(plugin);
        this.noWebCheck = new NoWebCheck(plugin);
        this.fastLadderCheck = new FastLadderCheck(plugin);
        this.elytraFlyCheck = new ElytraFlyCheck(plugin);
        this.velocityCheck = new VelocityCheck(plugin);
    }

    public ElytraFlyCheck elytraFlyCheck() {
        return elytraFlyCheck;
    }

    public VelocityCheck velocityCheck() {
        return velocityCheck;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) {
            return;
        }
        boolean moved = from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ();
        boolean rotated = from.getYaw() != to.getYaw() || from.getPitch() != to.getPitch();
        if (!moved && !rotated) {
            return;
        }

        PlayerData data = plugin.players().getOrCreate(player.getUniqueId(), player.getName());
        if (!data.isAnalyzed()) {
            return;
        }

        // Approximate flying packets when ProtocolLib is absent
        if (!plugin.packets().usesProtocolLib()) {
            if (moved && rotated) {
                plugin.packets().onPacket(player, PacketKind.POSITION_LOOK);
            } else if (moved) {
                plugin.packets().onPacket(player, PacketKind.POSITION);
            } else {
                plugin.packets().onPacket(player, PacketKind.LOOK);
            }
        }

        GameMode mode = player.getGameMode();
        if (mode == GameMode.CREATIVE || mode == GameMode.SPECTATOR || player.getAllowFlight() || player.isFlying()) {
            plugin.exemptions().exempt(player.getUniqueId(), ExemptReason.FLIGHT_ALLOWED, 500L);
            plugin.setbacks().recordIfSafe(player, data);
            return;
        }

        applyEnvironmentalExemptions(player);

        double dx = to.getX() - from.getX();
        double dy = to.getY() - from.getY();
        double dz = to.getZ() - from.getZ();
        data.setLastDelta(dx, dy, dz);
        data.setLastRotation(to.getYaw(), to.getPitch());
        data.setOnGround(player.isOnGround());
        data.latency().recordPacket();

        double horizontal = Math.hypot(dx, dz);
        double tolerance = data.latency().movementToleranceMultiplier();
        double tps = plugin.metrics().tps();
        if (tps < plugin.config().tpsSoftThreshold()) {
            tolerance += (plugin.config().tpsSoftThreshold() - tps) * 0.08;
        }

        if (moved) {
            speedCheck.handle(player, data, horizontal, dy, tolerance);
            flyCheck.handle(player, data, dy, tolerance);
            noFallCheck.handle(player, data, dy);
            jesusCheck.handle(player, data, dy, horizontal);
            spiderCheck.handle(player, data, dy, horizontal);
            stepCheck.handle(player, data, dy);
            phaseCheck.handle(player, data);
            noSlowCheck.handle(player, data, horizontal);
            airJumpCheck.handle(player, data, dy);
            inventoryMoveCheck.handle(player, data, horizontal);
            noWebCheck.handle(player, data, horizontal);
            fastLadderCheck.handle(player, data, dy);
            elytraFlyCheck.handle(player, data, horizontal, dy);
            velocityCheck.onMove(player, data, horizontal);
        }

        plugin.setbacks().recordIfSafe(player, data);
    }

    private void applyEnvironmentalExemptions(Player player) {
        if (player.isInsideVehicle()) {
            plugin.exemptions().exempt(player.getUniqueId(), ExemptReason.VEHICLE, 500L);
        }
        if (player.isGliding()) {
            plugin.exemptions().exempt(player.getUniqueId(), ExemptReason.ELYTRA, 200L);
        }
        if (player.isRiptiding()) {
            plugin.exemptions().exempt(player.getUniqueId(), ExemptReason.RIPTIDE, 1000L);
        }
        if (player.hasPotionEffect(PotionEffectType.LEVITATION)) {
            plugin.exemptions().exempt(player.getUniqueId(), ExemptReason.LEVITATION, 500L);
        }
        if (player.hasPotionEffect(PotionEffectType.SLOW_FALLING)) {
            plugin.exemptions().exempt(player.getUniqueId(), ExemptReason.SLOW_FALLING, 500L);
        }
        Material below = player.getLocation().clone().subtract(0, 0.2, 0).getBlock().getType();
        String name = below.name();
        if (name.contains("SLIME")) {
            plugin.exemptions().exempt(player.getUniqueId(), ExemptReason.SLIME, 1200L);
        }
        if (name.contains("HONEY")) {
            plugin.exemptions().exempt(player.getUniqueId(), ExemptReason.HONEY, 800L);
        }
        if (player.isInWater() || name.contains("WATER")) {
            plugin.exemptions().exempt(player.getUniqueId(), "speed", ExemptReason.WATER, 400L);
            plugin.exemptions().exempt(player.getUniqueId(), "fly", ExemptReason.WATER, 400L);
        }
        if (name.contains("LAVA") || player.getLocation().getBlock().getType().name().contains("LAVA")) {
            plugin.exemptions().exempt(player.getUniqueId(), ExemptReason.LAVA, 400L);
        }
        if (name.contains("LADDER") || name.contains("VINE") || name.contains("SCAFFOLD")
                || name.contains("WEEPING") || name.contains("TWISTING")) {
            plugin.exemptions().exempt(player.getUniqueId(), ExemptReason.CLIMBABLE, 600L);
        }
        if (name.contains("COBWEB") || name.contains("WEB")) {
            plugin.exemptions().exempt(player.getUniqueId(), ExemptReason.WEB, 400L);
        }
        Vector velocity = player.getVelocity();
        if (velocity.lengthSquared() > 0.35) {
            plugin.exemptions().exempt(player.getUniqueId(), ExemptReason.KNOCKBACK,
                    plugin.config().knockbackExemptMs(), "velocity");
        }
    }
}
