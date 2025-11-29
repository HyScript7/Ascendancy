package io.github.hyscript7.ascendancy;

import io.github.hyscript7.ascendancy.data.factions.FactionManager;
import io.github.hyscript7.ascendancy.data.factions.commands.FactionCommand;
import io.github.hyscript7.ascendancy.data.factions.simple.FactionItemInteractionListener;
import io.github.hyscript7.ascendancy.data.players.PlayerDataListener;
import io.github.hyscript7.ascendancy.data.players.storage.JsonPlayerDataStorage;
import io.github.hyscript7.ascendancy.data.players.PlayerDataManager;
import io.github.hyscript7.ascendancy.data.players.storage.PlayerDataStorage;
import io.github.hyscript7.ascendancy.data.players.names.TrueNameManager;
import io.github.hyscript7.ascendancy.features.innate.names.listeners.InnateCommandListener;
import io.github.hyscript7.ascendancy.features.voidrealm.listeners.*;
import io.github.hyscript7.ascendancy.registries.RegistryManager;
import org.bukkit.Bukkit;
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

        RegistryManager.getInstance().initialize();
        getLogger().info("Registries initialized successfully");

        registerListeners();
        getLogger().info("Listeners registered successfully");


        FactionManager.initialize();
        // add debug faction command
        this.registerCommand("faction", new FactionCommand());

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

    private void registerListeners() {
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerDataListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new InnateCommandListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new VoidRealmRespawnListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new DeadPlayerRestrictionListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new VoidRealmLayerChanger(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new VoidRealmStateListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new VoidRealmEffects(), this);
        getServer().getPluginManager().registerEvents(new FactionItemInteractionListener(), this);
    }

    public static AscendancyPlugin getInstance() {
        if (instance == null) {
            throw new NotInitializedException("The plugin hasn't been initialized yet!");
        }
        return instance;
    }
}
