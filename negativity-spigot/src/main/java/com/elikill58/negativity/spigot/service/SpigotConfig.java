package com.elikill58.negativity.spigot.service;

import com.elikill58.negativity.common.violation.CheckThresholds;
import com.elikill58.negativity.spigot.NegativityPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class SpigotConfig {

    private final NegativityPlugin plugin;
    private FileConfiguration config;
    private FileConfiguration checks;
    private FileConfiguration messages;
    private FileConfiguration punishments;

    public SpigotConfig(NegativityPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        this.config = loadYaml("config.yml");
        this.checks = loadYaml("checks.yml");
        this.messages = loadYaml("messages.yml");
        this.punishments = loadYaml("punishments.yml");
    }

    private FileConfiguration loadYaml(String name) {
        File file = new File(plugin.getDataFolder(), name);
        if (!file.exists() && plugin.getResource(name) != null) {
            plugin.saveResource(name, false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public boolean debug() {
        return config.getBoolean("debug", false);
    }

    public String language() {
        return config.getString("language", "pt_BR");
    }

    public String serverName() {
        return config.getString("server-name", "server");
    }

    public long tickInterval() {
        return config.getLong("performance.tick-interval", 20L);
    }

    public double tpsSoftThreshold() {
        return config.getDouble("tps.soft-threshold", 19.0);
    }

    public double tpsHardThreshold() {
        return config.getDouble("tps.hard-threshold", 15.0);
    }

    public long joinExemptMs() {
        return config.getLong("exemptions.join-ms", 8000L);
    }

    public long teleportExemptMs() {
        return config.getLong("exemptions.teleport-ms", 2000L);
    }

    public long respawnExemptMs() {
        return config.getLong("exemptions.respawn-ms", 2000L);
    }

    public long worldChangeExemptMs() {
        return config.getLong("exemptions.world-change-ms", 3000L);
    }

    public long knockbackExemptMs() {
        return config.getLong("exemptions.knockback-ms", 750L);
    }

    public boolean alertsEnabled() {
        return config.getBoolean("alerts.enabled", true);
    }

    public long alertCooldownMs() {
        return config.getLong("alerts.cooldown-ms", 1000L);
    }

    public String alertPermission() {
        return config.getString("alerts.permission", "negativity.alert");
    }

    public String alertFormat() {
        return config.getString("alerts.format",
                "&c[Negativity] &e%player% &7falhou &c%check% %subcheck% &7| VL: &f%vl% &7| Buffer: &f%buffer% &7| Ping: &f%ping%ms &7| TPS: &f%tps%");
    }

    public boolean isCheckEnabled(String id) {
        return checks.getBoolean("checks." + id.toLowerCase(Locale.ROOT) + ".enabled", true);
    }

    public String checkDisplayName(String id, String fallback) {
        return checks.getString("checks." + id.toLowerCase(Locale.ROOT) + ".exact_name", fallback);
    }

    public CheckThresholds thresholdsFor(String id) {
        String path = "checks." + id.toLowerCase(Locale.ROOT);
        ConfigurationSection section = checks.getConfigurationSection(path);
        if (section == null) {
            return CheckThresholds.defaults();
        }
        return new CheckThresholds(
                section.getDouble("alert-vl", 5.0),
                section.getDouble("setback-vl", 10.0),
                section.getDouble("punish-vl", 25.0),
                section.getDouble("buffer-decay", 0.5),
                section.getDouble("buffer-max", 50.0),
                section.getLong("alert-cooldown-ms", 1000L),
                section.getDouble("weight", 1.0),
                section.getBoolean("cancel", false),
                section.getBoolean("setback", true),
                section.getBoolean("log-only", false)
        );
    }

    public double checkDouble(String id, String key, double def) {
        return checks.getDouble("checks." + id.toLowerCase(Locale.ROOT) + "." + key, def);
    }

    public List<String> checkCommands(String id) {
        List<String> list = checks.getStringList("checks." + id.toLowerCase(Locale.ROOT) + ".commands");
        if (list == null || list.isEmpty()) {
            return punishments.getStringList("default-commands");
        }
        return list;
    }

    public boolean punishmentsEnabled() {
        return punishments.getBoolean("enabled", false);
    }

    public String storageType() {
        return config.getString("storage.type", "sqlite");
    }

    public String sqliteFile() {
        return config.getString("storage.sqlite-file", "violations.db");
    }

    public boolean proxyEnabled() {
        return config.getBoolean("proxy.enabled", true);
    }

    public String proxyChannel() {
        return config.getString("proxy.channel", "negativity:msg");
    }

    public boolean proxyRequireServerSender() {
        return config.getBoolean("proxy.require-server-sender", true);
    }

    public String message(String key) {
        String prefix = color(messages.getString("prefix", "&c[Negativity] &r"));
        String raw = messages.getString(key, key);
        return color(raw.replace("%prefix%", prefix));
    }

    public static String color(String input) {
        if (input == null) {
            return "";
        }
        return input.replace('&', '§');
    }

    public FileConfiguration rawConfig() {
        return config;
    }

    public FileConfiguration rawChecks() {
        return checks;
    }
}
