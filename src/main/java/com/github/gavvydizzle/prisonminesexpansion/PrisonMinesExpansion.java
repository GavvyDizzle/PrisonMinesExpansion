package com.github.gavvydizzle.prisonminesexpansion;

import com.github.gavvydizzle.prisonmines.api.PrisonMinesAPI;
import com.github.gavvydizzle.prisonminesexpansion.commands.AdminCommandManager;
import com.github.gavvydizzle.prisonminesexpansion.luckymine.LuckyMineManager;
import com.github.mittenmc.serverutils.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

public final class PrisonMinesExpansion extends JavaPlugin {

    private static PrisonMinesExpansion instance;
    private static ConfigManager configManager;

    @Override
    public void onEnable() {
        setupConfigFiles();

        instance = this;
        PrisonMinesAPI prisonMinesAPI = PrisonMinesAPI.getInstance();

        LuckyMineManager luckyMineManager = new LuckyMineManager(instance, prisonMinesAPI);
        getServer().getPluginManager().registerEvents(luckyMineManager, this);

        new AdminCommandManager(getCommand("minesexpansion"), luckyMineManager);

        // Save all configs after everything is done loading
        configManager.saveAll();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void setupConfigFiles() {
        configManager = new ConfigManager(this, Set.of("luckyMine"));
    }

    public static PrisonMinesExpansion getInstance() {
        return instance;
    }

    public static ConfigManager getConfigManager() {
        return configManager;
    }
}
