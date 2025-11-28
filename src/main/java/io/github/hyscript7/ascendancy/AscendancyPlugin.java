package io.github.hyscript7.ascendancy;

import io.github.hyscript7.ascendancy.data.JsonPlayerDataStorage;
import io.github.hyscript7.ascendancy.data.PlayerDataManager;
import io.github.hyscript7.ascendancy.data.PlayerDataStorage;
import io.github.hyscript7.ascendancy.data.truenames.TrueNameManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class AscendancyPlugin extends JavaPlugin {
    private static AscendancyPlugin instance;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("Plugin initializing");

        saveDefaultConfig();

        PlayerDataStorage playerStorage = new JsonPlayerDataStorage(this);

        PlayerDataManager.initialize(this, playerStorage);
        TrueNameManager.initialize(this);
        getLogger().info("Player data system initialized");

        TrueNameManager.getInstance().indexAllTrueNames(
                PlayerDataManager.getInstance().getAllData()
        );
        getLogger().info("True Names indexed successfully");

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
