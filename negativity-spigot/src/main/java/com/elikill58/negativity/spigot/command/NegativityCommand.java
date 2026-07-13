package com.elikill58.negativity.spigot.command;

import com.elikill58.negativity.api.exempt.ExemptReason;
import com.elikill58.negativity.api.violation.ViolationRecord;
import com.elikill58.negativity.common.player.PlayerData;
import com.elikill58.negativity.spigot.NegativityPlugin;
import com.elikill58.negativity.spigot.service.SpigotConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class NegativityCommand implements CommandExecutor, TabCompleter {

    private final NegativityPlugin plugin;

    public NegativityCommand(NegativityPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(SpigotConfig.color("&c[Negativity] &7/" + label + " <alerts|check|violations|debug|reload|info|exempt|logs>"));
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        return switch (sub) {
            case "alerts" -> handleAlerts(sender);
            case "check" -> handleCheck(sender, args);
            case "violations", "vl" -> handleViolations(sender, args);
            case "debug" -> handleDebug(sender, args);
            case "reload" -> handleReload(sender);
            case "info" -> handleInfo(sender);
            case "exempt" -> handleExempt(sender, args);
            case "logs" -> handleLogs(sender, args);
            default -> {
                sender.sendMessage(SpigotConfig.color("&c[Negativity] &7Subcomando desconhecido."));
                yield true;
            }
        };
    }

    private boolean handleAlerts(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players.");
            return true;
        }
        if (!player.hasPermission("negativity.alert") && !player.hasPermission("negativity.alerts")) {
            player.sendMessage(plugin.config().message("no-permission"));
            return true;
        }
        boolean next = !plugin.alerts().hasAlerts(player);
        plugin.alerts().setAlerts(player, next);
        player.sendMessage(plugin.config().message(next ? "alerts-enabled" : "alerts-disabled"));
        return true;
    }

    private boolean handleCheck(CommandSender sender, String[] args) {
        if (!sender.hasPermission("negativity.negativity") && !sender.hasPermission("negativity.check")) {
            sender.sendMessage(plugin.config().message("no-permission"));
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(SpigotConfig.color("&cUsage: /negativity check <player>"));
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.config().message("player-not-found"));
            return true;
        }
        PlayerData data = plugin.players().getOrCreate(target.getUniqueId(), target.getName());
        sender.sendMessage(SpigotConfig.color("&c[Negativity] &e" + target.getName()));
        sender.sendMessage(SpigotConfig.color("&7Ping: &f" + data.ping() + "ms &7Jitter: &f"
                + String.format(Locale.ROOT, "%.1f", data.jitter())
                + " &7TPS: &f" + String.format(Locale.ROOT, "%.2f", plugin.metrics().tps())));
        sender.sendMessage(SpigotConfig.color("&7World: &f" + data.worldName()
                + " &7Analyzed: &f" + data.isAnalyzed()
                + " &7Bedrock: &f" + data.isBedrock()));
        for (var info : plugin.checks().all()) {
            double vl = plugin.violations().getVl(target.getUniqueId(), info.id());
            double buffer = plugin.violations().getBuffer(target.getUniqueId(), info.id());
            if (vl > 0 || buffer > 0) {
                sender.sendMessage(SpigotConfig.color("&7- &e" + info.displayName()
                        + " &7VL=&f" + String.format(Locale.ROOT, "%.1f", vl)
                        + " &7Buffer=&f" + String.format(Locale.ROOT, "%.1f", buffer)));
            }
        }
        return true;
    }

    private boolean handleViolations(CommandSender sender, String[] args) {
        if (!sender.hasPermission("negativity.negativity")) {
            sender.sendMessage(plugin.config().message("no-permission"));
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(SpigotConfig.color("&cUsage: /negativity violations <player>"));
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.config().message("player-not-found"));
            return true;
        }
        List<ViolationRecord> recent = plugin.violations().recent(target.getUniqueId(), 10);
        if (recent.isEmpty()) {
            sender.sendMessage(SpigotConfig.color("&c[Negativity] &7Nenhuma violação recente."));
            return true;
        }
        for (ViolationRecord r : recent) {
            sender.sendMessage(SpigotConfig.color("&7* &e" + r.checkId() + " " + r.subcheck()
                    + " &7VL=&f" + String.format(Locale.ROOT, "%.1f", r.vl())
                    + " &7ping=&f" + r.ping()));
        }
        return true;
    }

    private boolean handleDebug(CommandSender sender, String[] args) {
        if (!sender.hasPermission("negativity.admin") && !sender.hasPermission("negativity.negativity")) {
            sender.sendMessage(plugin.config().message("no-permission"));
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(SpigotConfig.color("&cUsage: /negativity debug <player> <check>"));
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.config().message("player-not-found"));
            return true;
        }
        String check = args[2].toLowerCase(Locale.ROOT);
        var exemptions = plugin.exemptions().debugExemptions(target.getUniqueId(), check);
        sender.sendMessage(plugin.config().message("debug-header")
                .replace("%player%", target.getName())
                .replace("%check%", check));
        if (exemptions.isEmpty()) {
            sender.sendMessage(SpigotConfig.color("&7Sem isenções ativas para este check."));
        } else {
            for (String reason : exemptions) {
                sender.sendMessage(SpigotConfig.color("&7- isento: &e" + reason));
            }
        }
        double vl = plugin.violations().getVl(target.getUniqueId(), check);
        double buffer = plugin.violations().getBuffer(target.getUniqueId(), check);
        sender.sendMessage(SpigotConfig.color("&7VL=&f" + vl + " &7Buffer=&f" + buffer));
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("negativity.admin") && !sender.hasPermission("negativity.reload")) {
            sender.sendMessage(plugin.config().message("no-permission"));
            return true;
        }
        plugin.reloadSafe();
        sender.sendMessage(plugin.config().message("reload-done"));
        sender.sendMessage(plugin.config().message("reload-partial"));
        return true;
    }

    private boolean handleInfo(CommandSender sender) {
        sender.sendMessage(SpigotConfig.color("&c[Negativity] &7v" + plugin.getDescription().getVersion()));
        sender.sendMessage(SpigotConfig.color("&7MC: &f" + plugin.serverVersion()
                + " &7Java: &f" + System.getProperty("java.version")));
        sender.sendMessage(SpigotConfig.color("&7Checks: &f" + plugin.checks().all().size()
                + " &7Players: &f" + plugin.players().online().size()));
        sender.sendMessage(SpigotConfig.color("&7TPS: &f" + String.format(Locale.ROOT, "%.2f", plugin.metrics().tps())
                + " &7MSPT~: &f" + String.format(Locale.ROOT, "%.1f", plugin.metrics().mspt())));
        return true;
    }

    private boolean handleExempt(CommandSender sender, String[] args) {
        if (!sender.hasPermission("negativity.admin") && !sender.hasPermission("negativity.exempt")) {
            sender.sendMessage(plugin.config().message("no-permission"));
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(SpigotConfig.color("&cUsage: /negativity exempt <player> [seconds]"));
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.config().message("player-not-found"));
            return true;
        }
        long seconds = 30;
        if (args.length >= 3) {
            try {
                seconds = Long.parseLong(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(SpigotConfig.color("&cSegundos inválidos."));
                return true;
            }
        }
        plugin.exemptions().exempt(target.getUniqueId(), ExemptReason.MANUAL, seconds * 1000L, "by " + sender.getName());
        sender.sendMessage(plugin.config().message("exempt-added")
                .replace("%player%", target.getName())
                .replace("%reason%", "manual " + seconds + "s"));
        return true;
    }

    private boolean handleLogs(CommandSender sender, String[] args) {
        return handleViolations(sender, args);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return filter(Arrays.asList("alerts", "check", "violations", "debug", "reload", "info", "exempt", "logs"), args[0]);
        }
        if (args.length == 2 && List.of("check", "violations", "debug", "exempt", "logs", "vl").contains(args[0].toLowerCase(Locale.ROOT))) {
            return filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()), args[1]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("debug")) {
            return filter(plugin.checks().all().stream().map(c -> c.id()).collect(Collectors.toList()), args[2]);
        }
        return List.of();
    }

    private static List<String> filter(List<String> options, String token) {
        String t = token.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (String o : options) {
            if (o.toLowerCase(Locale.ROOT).startsWith(t)) {
                out.add(o);
            }
        }
        return out;
    }
}
