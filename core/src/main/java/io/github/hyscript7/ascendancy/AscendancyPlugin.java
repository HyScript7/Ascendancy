package io.github.hyscript7.ascendancy;

import io.github.hyscript7.ascendancy.api.AscendancyAPI;
import io.github.hyscript7.ascendancy.api.data.Persistence;
import io.github.hyscript7.ascendancy.api.events.AscendancyDisabledEvent;
import io.github.hyscript7.ascendancy.api.events.AscendancyEnabledEvent;
import io.github.hyscript7.ascendancy.data.BasePersistence;
import io.github.hyscript7.ascendancy.data.DataLifecycleListener;
import io.github.hyscript7.ascendancy.data.backend.JsonFileStorageBackend;
import io.github.hyscript7.ascendancy.data.store.BaseDataStore;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class AscendancyPlugin extends JavaPlugin implements AscendancyAPI {
    /** How often dirty entities are written out, when the config does not say otherwise. */
    private static final long DEFAULT_AUTOSAVE_SECONDS = 300L;

    /** How long shutdown waits for pending writes before deciding the disk is not coming back. */
    private static final long SHUTDOWN_TIMEOUT_SECONDS = 30L;

    private static final long TICKS_PER_SECOND = 20L;

    private BasePersistence persistence;
    private BukkitTask autosaveTask;

    @Override
    public void onEnable() {
        long coldReadWarnMillis =
                getConfig().getLong("persistence.cold-read-warn-millis", BaseDataStore.DEFAULT_COLD_READ_WARN_MILLIS);
        persistence = new BasePersistence(
                new JsonFileStorageBackend(getDataFolder().toPath().resolve("data")), coldReadWarnMillis);
        AscendancyAPI.set(this, this);
        Bukkit.getPluginManager().registerEvents(new DataLifecycleListener(persistence.store()), this);

        Bukkit.getPluginManager().callEvent(new AscendancyEnabledEvent(this));

        // Only now, once packs have had their chance to register scopes of their own, is it safe to
        // populate the eager ones.
        persistence.loadEagerScopes();
        startAutosave();
    }

    @Override
    public Persistence persistence() {
        return persistence;
    }

    @Override
    public void onDisable() {
        Bukkit.getPluginManager().callEvent(new AscendancyDisabledEvent());
        // All remaining code goes under disable event, event is called when plugin starts shutting down

        if (autosaveTask != null) {
            autosaveTask.cancel();
        }
        if (persistence != null) {
            // Packs have just done their teardown writes, so this has to come after the event.
            persistence.shutdown(SHUTDOWN_TIMEOUT_SECONDS);
        }
    }

    /**
     * Schedules the periodic flush. Runs on the main thread so that snapshots are taken while
     * gameplay code is not mutating entities; only the writing itself moves off it.
     */
    private void startAutosave() {
        long seconds = getConfig().getLong("persistence.autosave-interval-seconds", DEFAULT_AUTOSAVE_SECONDS);
        if (seconds <= 0) {
            getSLF4JLogger().warn("Persistence autosave is disabled; data will only be written at shutdown");
            return;
        }
        long ticks = seconds * TICKS_PER_SECOND;
        autosaveTask = Bukkit.getScheduler()
                .runTaskTimer(this, () -> persistence.store().flushAll(), ticks, ticks);
    }
}
