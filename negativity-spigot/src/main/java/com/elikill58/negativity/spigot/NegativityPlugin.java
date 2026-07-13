package com.elikill58.negativity.spigot;

import com.elikill58.negativity.api.NegativityAPI;
import com.elikill58.negativity.api.check.CheckCategory;
import com.elikill58.negativity.common.check.CheckDefinition;
import com.elikill58.negativity.common.check.CheckManager;
import com.elikill58.negativity.common.config.ConfigDefaults;
import com.elikill58.negativity.common.exempt.ExemptionManager;
import com.elikill58.negativity.common.player.PlayerDataManager;
import com.elikill58.negativity.common.storage.FileViolationStorage;
import com.elikill58.negativity.common.storage.NoOpViolationStorage;
import com.elikill58.negativity.common.storage.SqliteViolationStorage;
import com.elikill58.negativity.common.storage.ViolationStorage;
import com.elikill58.negativity.common.version.MinecraftVersion;
import com.elikill58.negativity.common.violation.CheckThresholds;
import com.elikill58.negativity.common.violation.ViolationManager;
import com.elikill58.negativity.spigot.command.NegativityCommand;
import com.elikill58.negativity.spigot.listener.CombatAndInteractListener;
import com.elikill58.negativity.spigot.listener.PlayerConnectionListener;
import com.elikill58.negativity.spigot.listener.PlayerMoveCheckListener;
import com.elikill58.negativity.spigot.packet.PacketService;
import com.elikill58.negativity.spigot.service.AlertService;
import com.elikill58.negativity.spigot.service.CheckSupport;
import com.elikill58.negativity.spigot.service.ProxyBridge;
import com.elikill58.negativity.spigot.service.ServerMetrics;
import com.elikill58.negativity.spigot.service.SetbackService;
import com.elikill58.negativity.spigot.service.SpigotConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

public final class NegativityPlugin extends JavaPlugin {

    private static NegativityPlugin instance;

    private ViolationManager violationManager;
    private PlayerDataManager playerDataManager;
    private ExemptionManager exemptionManager;
    private CheckManager checkManager;
    private SpigotConfig config;
    private AlertService alertService;
    private SetbackService setbackService;
    private ServerMetrics serverMetrics;
    private CheckSupport checkSupport;
    private PacketService packetService;
    private ProxyBridge proxyBridge;
    private ViolationStorage storage;
    private PlayerMoveCheckListener moveListener;
    private MinecraftVersion serverVersion;

    public static NegativityPlugin get() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        serverVersion = MinecraftVersion.fromBukkitVersion(Bukkit.getBukkitVersion());
        getLogger().info("Detected Minecraft version: " + serverVersion + " (bukkit=" + Bukkit.getBukkitVersion() + ")");

        saveDefaultConfigs();
        this.config = new SpigotConfig(this);
        this.config.load();

        this.violationManager = new ViolationManager(128);
        this.playerDataManager = new PlayerDataManager(violationManager);
        this.exemptionManager = new ExemptionManager();
        this.checkManager = new CheckManager();
        registerDefaultChecks();

        this.serverMetrics = new ServerMetrics();
        this.alertService = new AlertService(this, config);
        this.setbackService = new SetbackService(this);
        this.checkSupport = new CheckSupport(this);
        this.packetService = new PacketService(this);
        this.proxyBridge = new ProxyBridge(this);
        this.storage = createStorage();
        this.storage.start();

        NegativityAPI.setInstance(new NegativityAPI(
                playerDataManager,
                violationManager,
                checkManager,
                exemptionManager
        ));

        this.moveListener = new PlayerMoveCheckListener(this);
        Bukkit.getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        Bukkit.getPluginManager().registerEvents(moveListener, this);
        Bukkit.getPluginManager().registerEvents(new CombatAndInteractListener(this, moveListener), this);

        packetService.start();
        proxyBridge.register();

        var command = getCommand("negativity");
        if (command != null) {
            NegativityCommand executor = new NegativityCommand(this);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            playerDataManager.getOrCreate(player.getUniqueId(), player.getName());
            exemptionManager.exempt(player.getUniqueId(),
                    com.elikill58.negativity.api.exempt.ExemptReason.RELOAD, 3000L);
        }

        long tickInterval = Math.max(1L, config.tickInterval());
        Bukkit.getScheduler().runTaskTimer(this, this::globalTick, tickInterval, tickInterval);
        Bukkit.getScheduler().runTaskTimer(this, serverMetrics::sample, 1L, 1L);

        getLogger().info("Negativity " + getDescription().getVersion() + " enabled with "
                + checkManager.all().size() + " checks. ProtocolLib=" + packetService.usesProtocolLib()
                + " storage=" + config.storageType());
    }

    @Override
    public void onDisable() {
        try {
            if (packetService != null) {
                packetService.stop();
            }
            if (proxyBridge != null) {
                proxyBridge.unregister();
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                playerDataManager.remove(player.getUniqueId());
                exemptionManager.removePlayer(player.getUniqueId());
            }
            if (storage != null) {
                storage.close();
            }
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Error while disabling Negativity", e);
        }
        NegativityAPI.setInstance(null);
        instance = null;
        getLogger().info("Negativity disabled.");
    }

    private ViolationStorage createStorage() {
        String type = config.storageType().toLowerCase();
        return switch (type) {
            case "sqlite" -> new SqliteViolationStorage(
                    getDataFolder().toPath().resolve(config.sqliteFile()),
                    getLogger()
            );
            case "file" -> new FileViolationStorage(
                    getDataFolder().toPath().resolve("logs"),
                    getLogger()
            );
            case "none", "off" -> new NoOpViolationStorage();
            default -> {
                getLogger().warning("Unknown storage type '" + type + "', using sqlite");
                yield new SqliteViolationStorage(
                        getDataFolder().toPath().resolve(config.sqliteFile()),
                        getLogger()
                );
            }
        };
    }

    private void globalTick() {
        exemptionManager.tickCleanup();
        double tps = serverMetrics.tps();
        if (tps < config.tpsHardThreshold()) {
            for (var data : playerDataManager.allData()) {
                exemptionManager.exempt(
                        data.uuid(),
                        com.elikill58.negativity.api.exempt.ExemptReason.LOW_TPS,
                        1500L,
                        "tps=" + String.format("%.2f", tps)
                );
            }
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerDataManager.getData(player.getUniqueId()).ifPresent(data -> {
                data.latency().recordPing(player.getPing());
                if (player.getWorld() != null) {
                    data.setWorldName(player.getWorld().getName());
                }
            });
        }
    }

    private void registerDefaultChecks() {
        Map<String, CheckCategory> defs = new LinkedHashMap<>();
        defs.put("speed", CheckCategory.MOVEMENT);
        defs.put("fly", CheckCategory.MOVEMENT);
        defs.put("timer", CheckCategory.PACKET);
        defs.put("nofall", CheckCategory.MOVEMENT);
        defs.put("jesus", CheckCategory.MOVEMENT);
        defs.put("spider", CheckCategory.MOVEMENT);
        defs.put("step", CheckCategory.MOVEMENT);
        defs.put("phase", CheckCategory.MOVEMENT);
        defs.put("noslow", CheckCategory.MOVEMENT);
        defs.put("airjump", CheckCategory.MOVEMENT);
        defs.put("noweb", CheckCategory.MOVEMENT);
        defs.put("fastladder", CheckCategory.MOVEMENT);
        defs.put("elytrafly", CheckCategory.MOVEMENT);
        defs.put("inventorymove", CheckCategory.PLAYER);
        defs.put("reach", CheckCategory.COMBAT);
        defs.put("killaura", CheckCategory.COMBAT);
        defs.put("autoclick", CheckCategory.COMBAT);
        defs.put("criticals", CheckCategory.COMBAT);
        defs.put("velocity", CheckCategory.COMBAT);
        defs.put("scaffold", CheckCategory.WORLD);
        defs.put("nuker", CheckCategory.WORLD);
        defs.put("fastplace", CheckCategory.WORLD);
        defs.put("fastbow", CheckCategory.COMBAT);
        defs.put("regen", CheckCategory.PLAYER);
        defs.put("badpackets", CheckCategory.PACKET);

        for (var e : defs.entrySet()) {
            String id = e.getKey();
            String name = config.checkDisplayName(id, capitalize(id));
            checkManager.register(new CheckDefinition(
                    id, name, e.getValue(), config.isCheckEnabled(id), config.thresholdsFor(id)
            ));
        }
    }

    private static String capitalize(String id) {
        if (id.isEmpty()) {
            return id;
        }
        return Character.toUpperCase(id.charAt(0)) + id.substring(1);
    }

    private void saveDefaultConfigs() {
        saveResourceIfMissing("config.yml", ConfigDefaults.configYml());
        saveResourceIfMissing("checks.yml", ConfigDefaults.checksYml());
        saveResourceIfMissing("messages.yml", ConfigDefaults.messagesYml());
        saveResourceIfMissing("punishments.yml", ConfigDefaults.punishmentsYml());
    }

    private void saveResourceIfMissing(String name, String defaultContent) {
        java.io.File file = new java.io.File(getDataFolder(), name);
        if (file.exists()) {
            return;
        }
        if (getResource(name) != null) {
            saveResource(name, false);
            return;
        }
        try {
            if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
                getLogger().warning("Could not create data folder");
            }
            java.nio.file.Files.writeString(file.toPath(), defaultContent);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Could not write default " + name, e);
        }
    }

    public void reloadSafe() {
        config.load();
        for (var info : checkManager.all()) {
            checkManager.getDefinition(info.id()).ifPresent(def -> {
                def.setEnabled(config.isCheckEnabled(def.id()));
                def.setThresholds(config.thresholdsFor(def.id()));
            });
        }
        getLogger().info("Safe reload completed.");
    }

    public ViolationManager violations() {
        return violationManager;
    }

    public PlayerDataManager players() {
        return playerDataManager;
    }

    public ExemptionManager exemptions() {
        return exemptionManager;
    }

    public CheckManager checks() {
        return checkManager;
    }

    public SpigotConfig config() {
        return config;
    }

    public AlertService alerts() {
        return alertService;
    }

    public SetbackService setbacks() {
        return setbackService;
    }

    public ServerMetrics metrics() {
        return serverMetrics;
    }

    public CheckSupport checksSupport() {
        return checkSupport;
    }

    public PacketService packets() {
        return packetService;
    }

    public ProxyBridge proxy() {
        return proxyBridge;
    }

    public ViolationStorage storage() {
        return storage;
    }

    public PlayerMoveCheckListener moveListener() {
        return moveListener;
    }

    public MinecraftVersion serverVersion() {
        return serverVersion;
    }
}
