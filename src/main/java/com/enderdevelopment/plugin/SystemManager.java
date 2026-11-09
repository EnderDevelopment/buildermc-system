package com.enderdevelopment.plugin;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class SystemManager implements CommandExecutor {
    private static final Pattern DURATION = Pattern.compile("([1-9][0-9]*)([smhd])", Pattern.CASE_INSENSITIVE);
    private final JavaPlugin plugin;
    private final Set<UUID> vanishedPlayers = new HashSet<>();

    public SystemManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isVanished(Player player) {
        return vanishedPlayers.contains(player.getUniqueId());
    }

    public Set<Player> getVanishedPlayers() {
        Set<Player> players = new HashSet<>();
        for (UUID id : vanishedPlayers) {
            Player player = Bukkit.getPlayer(id);
            if (player != null) players.add(player);
        }
        return players;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (command.getName().toLowerCase(Locale.ROOT)) {
            case "setspawn" -> setSpawn(sender);
            case "spawn" -> teleportToSpawn(sender);
            case "vanish" -> toggleVanish(sender);
            case "report" -> report(sender, args);
            case "tempban" -> tempBan(sender, args, false);
            case "tempbanip" -> tempBan(sender, args, true);
            default -> { return false; }
        }
        return true;
    }

    private void setSpawn(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can set the spawn.", NamedTextColor.RED));
            return;
        }
        Location location = player.getLocation();
        plugin.getConfig().set("spawn.world", location.getWorld().getName());
        plugin.getConfig().set("spawn.x", location.getX());
        plugin.getConfig().set("spawn.y", location.getY());
        plugin.getConfig().set("spawn.z", location.getZ());
        plugin.getConfig().set("spawn.yaw", location.getYaw());
        plugin.getConfig().set("spawn.pitch", location.getPitch());
        plugin.saveConfig();
        player.sendMessage(Component.text("Spawn location saved.", NamedTextColor.GREEN));
    }

    private void teleportToSpawn(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return;
        }
        String worldName = plugin.getConfig().getString("spawn.world");
        if (worldName == null) {
            player.sendMessage(Component.text("Spawn has not been set yet.", NamedTextColor.RED));
            return;
        }
        org.bukkit.World world = Bukkit.getWorld(worldName);
        if (world == null) {
            player.sendMessage(Component.text("The spawn world is not available.", NamedTextColor.RED));
            return;
        }
        Location spawn = new Location(world, plugin.getConfig().getDouble("spawn.x"),
                plugin.getConfig().getDouble("spawn.y"), plugin.getConfig().getDouble("spawn.z"),
                (float) plugin.getConfig().getDouble("spawn.yaw"),
                (float) plugin.getConfig().getDouble("spawn.pitch"));
        player.teleport(spawn);
        player.sendMessage(Component.text("Teleported to spawn.", NamedTextColor.GREEN));
    }

    private void toggleVanish(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return;
        }
        if (vanishedPlayers.remove(player.getUniqueId())) {
            for (Player viewer : Bukkit.getOnlinePlayers()) viewer.showPlayer(plugin, player);
            player.sendMessage(Component.text("Vanish disabled.", NamedTextColor.YELLOW));
        } else {
            vanishedPlayers.add(player.getUniqueId());
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                if (!viewer.hasPermission("buildermc.vanish")) viewer.hidePlayer(plugin, player);
            }
            player.sendMessage(Component.text("Vanish enabled.", NamedTextColor.GREEN));
        }
    }

    private void report(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /report <player> <reason>", NamedTextColor.RED));
            return;
        }
        String reason = join(args, 1);
        String reporter = sender instanceof Player player ? player.getName() : sender.getName();
        Component message = Component.text("[Report] ", NamedTextColor.RED)
                .append(Component.text(reporter + " reported " + args[0] + ": " + reason, NamedTextColor.YELLOW));
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("buildermc.report.notify")) online.sendMessage(message);
        }
        plugin.getLogger().info("Report from " + reporter + " about " + args[0] + ": " + reason);
        sender.sendMessage(Component.text("Your report was sent to the staff team.", NamedTextColor.GREEN));
    }

    private void tempBan(CommandSender sender, String[] args, boolean ipBan) {
        String usage = ipBan ? "Usage: /tempbanip <player> <duration> [reason]"
                : "Usage: /tempban <player> <duration> [reason]";
        if (args.length < 2) {
            sender.sendMessage(Component.text(usage, NamedTextColor.RED));
            return;
        }
        Duration duration;
        try {
            duration = parseDuration(args[1]);
        } catch (IllegalArgumentException exception) {
            sender.sendMessage(Component.text("Duration must look like 30m, 2h, or 7d.", NamedTextColor.RED));
            return;
        }
        Instant expires = Instant.now().plus(duration);
        String reason = args.length > 2 ? join(args, 2) : "Temporary ban";
        String source = sender.getName();
        if (ipBan) {
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null || target.getAddress() == null || target.getAddress().getAddress() == null) {
                sender.sendMessage(Component.text("That player must be online to apply an IP ban.", NamedTextColor.RED));
                return;
            }
            String address = target.getAddress().getAddress().getHostAddress();
            Bukkit.getBanList(BanList.Type.IP).addBan(address, reason, java.util.Date.from(expires), source);
            target.kick(Component.text("Your IP is temporarily banned. Reason: " + reason, NamedTextColor.RED));
            sender.sendMessage(Component.text("IP-banned " + target.getName() + " for " + formatDuration(duration) + ".", NamedTextColor.GREEN));
        } else {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            Bukkit.getBanList(BanList.Type.NAME).addBan(target.getName() == null ? args[0] : target.getName(),
                    reason, java.util.Date.from(expires), source);
            Player onlineTarget = target.getPlayer();
            if (onlineTarget != null) {
                onlineTarget.kick(Component.text("You are temporarily banned. Reason: " + reason, NamedTextColor.RED));
            }
            sender.sendMessage(Component.text("Banned " + args[0] + " for " + formatDuration(duration) + ".", NamedTextColor.GREEN));
        }
    }

    private Duration parseDuration(String input) {
        Matcher matcher = DURATION.matcher(input);
        if (!matcher.matches()) throw new IllegalArgumentException("Invalid duration");
        long amount;
        try { amount = Long.parseLong(matcher.group(1)); }
        catch (NumberFormatException exception) { throw new IllegalArgumentException("Invalid duration"); }
        long secondsPerUnit = switch (matcher.group(2).toLowerCase(Locale.ROOT)) {
            case "s" -> 1L;
            case "m" -> 60L;
            case "h" -> 3600L;
            case "d" -> 86400L;
            default -> throw new IllegalArgumentException("Invalid duration");
        };
        if (amount > Long.MAX_VALUE / secondsPerUnit) throw new IllegalArgumentException("Invalid duration");
        return Duration.ofSeconds(amount * secondsPerUnit);
    }

    private String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        if (seconds % 86400 == 0) return (seconds / 86400) + "d";
        if (seconds % 3600 == 0) return (seconds / 3600) + "h";
        if (seconds % 60 == 0) return (seconds / 60) + "m";
        return seconds + "s";
    }

    private String join(String[] args, int start) {
        List<String> parts = new ArrayList<>();
        for (int i = start; i < args.length; i++) parts.add(args[i]);
        return String.join(" ", parts);
    }
}
