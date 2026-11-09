package com.enderdevelopment.plugin;

import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    @Override
    public void onEnable() {
        saveDefaultConfig();
        SystemManager manager = new SystemManager(this);
        getCommand("setspawn").setExecutor(manager);
        getCommand("vanish").setExecutor(manager);
        getCommand("report").setExecutor(manager);
        getCommand("spawn").setExecutor(manager);
        getCommand("tempban").setExecutor(manager);
        getCommand("tempbanip").setExecutor(manager);
        getServer().getPluginManager().registerEvents(new SystemListener(this, manager), this);
        getLogger().info("Buildermc System enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Buildermc System disabled.");
    }
}
