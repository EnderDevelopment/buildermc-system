package com.enderdevelopment.plugin;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class SystemListener implements Listener {
    private final JavaPlugin plugin;
    private final SystemManager manager;

    public SystemListener(JavaPlugin plugin, SystemManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player joining = event.getPlayer();
        for (Player vanished : manager.getVanishedPlayers()) {
            if (!joining.hasPermission("buildermc.vanish")) {
                joining.hidePlayer(plugin, vanished);
            }
        }
        if (manager.isVanished(joining)) {
            event.joinMessage(null);
        }
    }
}
