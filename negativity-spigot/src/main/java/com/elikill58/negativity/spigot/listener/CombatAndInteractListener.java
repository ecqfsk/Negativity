package com.elikill58.negativity.spigot.listener;

import com.elikill58.negativity.common.packet.PacketKind;
import com.elikill58.negativity.common.player.PlayerData;
import com.elikill58.negativity.spigot.NegativityPlugin;
import com.elikill58.negativity.spigot.check.AutoClickCheck;
import com.elikill58.negativity.spigot.check.CriticalsCheck;
import com.elikill58.negativity.spigot.check.FastBowCheck;
import com.elikill58.negativity.spigot.check.FastPlaceCheck;
import com.elikill58.negativity.spigot.check.KillAuraCheck;
import com.elikill58.negativity.spigot.check.NukerCheck;
import com.elikill58.negativity.spigot.check.ReachCheck;
import com.elikill58.negativity.spigot.check.RegenCheck;
import com.elikill58.negativity.spigot.check.ScaffoldCheck;
import com.elikill58.negativity.spigot.check.VelocityCheck;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerRiptideEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.projectiles.ProjectileSource;

public final class CombatAndInteractListener implements Listener {

    private final NegativityPlugin plugin;
    private final ReachCheck reachCheck;
    private final KillAuraCheck killAuraCheck;
    private final AutoClickCheck autoClickCheck;
    private final CriticalsCheck criticalsCheck;
    private final ScaffoldCheck scaffoldCheck;
    private final NukerCheck nukerCheck;
    private final FastPlaceCheck fastPlaceCheck;
    private final FastBowCheck fastBowCheck;
    private final RegenCheck regenCheck;
    private final VelocityCheck velocityCheck;

    public CombatAndInteractListener(NegativityPlugin plugin, PlayerMoveCheckListener moveListener) {
        this.plugin = plugin;
        this.reachCheck = new ReachCheck(plugin);
        this.killAuraCheck = new KillAuraCheck(plugin);
        this.autoClickCheck = new AutoClickCheck(plugin);
        this.criticalsCheck = new CriticalsCheck(plugin);
        this.scaffoldCheck = new ScaffoldCheck(plugin);
        this.nukerCheck = new NukerCheck(plugin);
        this.fastPlaceCheck = new FastPlaceCheck(plugin);
        this.fastBowCheck = new FastBowCheck(plugin);
        this.regenCheck = new RegenCheck(plugin);
        this.velocityCheck = moveListener.velocityCheck();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        Player attacker = null;
        if (event.getDamager() instanceof Player p) {
            attacker = p;
        } else if (event.getDamager() instanceof Projectile proj && proj.getShooter() instanceof Player p) {
            attacker = p;
        }
        if (attacker == null) {
            return;
        }
        PlayerData data = plugin.players().getOrCreate(attacker.getUniqueId(), attacker.getName());
        data.markAttack();
        if (!plugin.packets().usesProtocolLib()) {
            plugin.packets().onPacket(attacker, PacketKind.USE_ENTITY);
        }
        Entity target = event.getEntity();
        if (event.getDamager() instanceof Player) {
            reachCheck.handle(attacker, data, target);
            killAuraCheck.handle(attacker, data, target);
            criticalsCheck.handle(attacker, data, event);
            autoClickCheck.onClick(attacker, data);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVelocity(PlayerVelocityEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.players().getOrCreate(player.getUniqueId(), player.getName());
        velocityCheck.onVelocity(player, data, event.getVelocity());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAnimate(PlayerAnimationEvent event) {
        if (event.getAnimationType() != PlayerAnimationType.ARM_SWING) {
            return;
        }
        Player player = event.getPlayer();
        PlayerData data = plugin.players().getOrCreate(player.getUniqueId(), player.getName());
        if (!plugin.packets().usesProtocolLib()) {
            plugin.packets().onPacket(player, PacketKind.ARM_ANIMATION);
        }
        autoClickCheck.onClick(player, data);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.players().getOrCreate(player.getUniqueId(), player.getName());
        if (!plugin.packets().usesProtocolLib()) {
            plugin.packets().onPacket(player, PacketKind.BLOCK_DIG);
        }
        nukerCheck.onBreak(player, data);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.players().getOrCreate(player.getUniqueId(), player.getName());
        if (!plugin.packets().usesProtocolLib()) {
            plugin.packets().onPacket(player, PacketKind.BLOCK_PLACE);
        }
        fastPlaceCheck.onPlace(player, data);
        scaffoldCheck.onPlace(player, data, event.getBlockPlaced());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBow(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        PlayerData data = plugin.players().getOrCreate(player.getUniqueId(), player.getName());
        fastBowCheck.onShoot(player, data);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProjectile(ProjectileLaunchEvent event) {
        ProjectileSource source = event.getEntity().getShooter();
        if (source instanceof Player player) {
            PlayerData data = plugin.players().getOrCreate(player.getUniqueId(), player.getName());
            // firework boost for elytra
            if (event.getEntity().getName().toLowerCase().contains("firework")
                    || event.getEntity().getType().name().contains("FIREWORK")) {
                plugin.moveListener().elytraFlyCheck().markBoost(data);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRegen(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (event.getRegainReason() != EntityRegainHealthEvent.RegainReason.SATIATED
                && event.getRegainReason() != EntityRegainHealthEvent.RegainReason.REGEN) {
            return;
        }
        PlayerData data = plugin.players().getOrCreate(player.getUniqueId(), player.getName());
        regenCheck.onRegen(player, data);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        // reserved for FastEat expansion; marks activity
        Player player = event.getPlayer();
        plugin.players().getOrCreate(player.getUniqueId(), player.getName());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (!plugin.packets().usesProtocolLib() && event.getAction().isRightClick()) {
            plugin.packets().onPacket(event.getPlayer(), PacketKind.BLOCK_PLACE);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRiptide(PlayerRiptideEvent event) {
        plugin.exemptions().exempt(event.getPlayer().getUniqueId(),
                com.elikill58.negativity.api.exempt.ExemptReason.RIPTIDE, 1500L);
    }
}
