package io.github.hyscript7.ascendancy;

import io.github.hyscript7.ascendancy.data.JsonPlayerDataStorage;
import io.github.hyscript7.ascendancy.data.PlayerDataManager;
import io.github.hyscript7.ascendancy.data.PlayerDataStorage;
import org.bukkit.plugin.java.JavaPlugin;

public final class AscendancyPlugin extends JavaPlugin {
    private static AscendancyPlugin instance;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("Plugin initializing");

        saveDefaultConfig();

        PlayerDataStorage playerStorage = new JsonPlayerDataStorage(getDataFolder());

        PlayerDataManager.initialize(this, playerStorage);
        getLogger().info("Player data system initialized");

        getLogger().info("Plugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        try {
            PlayerDataManager.getInstance().shutdown();
        } catch (NotInitializedException e) {
            getLogger().warning("PlayerDataManager hasn't been initialized yet!");
        }
        getLogger().info("Plugin disabled successfully!");
    }

    public static AscendancyPlugin getInstance() {
        if (instance == null) {
            throw new NotInitializedException("The plugin hasn't been initialized yet!");
        }
        return instance;
    }
}
