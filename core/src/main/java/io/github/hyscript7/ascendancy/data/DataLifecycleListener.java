package io.github.hyscript7.ascendancy.data;

import io.github.hyscript7.ascendancy.api.data.DataScopes;
import io.github.hyscript7.ascendancy.api.data.DataStore;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

/**
 * Drives residency for Core's own {@link io.github.hyscript7.ascendancy.api.data.ResidencyPolicy#LAZY}
 * scopes. Without this, lazily loaded entities would accumulate until the server ran out of memory.
 */
@Slf4j
public class DataLifecycleListener implements Listener {
    private final DataStore store;

    /**
     * @param store The store whose residency this listener drives
     * @throws IllegalArgumentException If the store is null
     */
    public DataLifecycleListener(DataStore store) {
        if (store == null) {
            throw new IllegalArgumentException("Store cannot be null");
        }
        this.store = store;
    }

    /**
     * Warms a joining player's data so the first feature to read it is not paying for a disk hit on
     * the main thread.
     *
     * @param event The join event
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        store.preload(DataScopes.of(event.getPlayer())).exceptionally(failure -> {
            log.error("Failed to preload data for {}", event.getPlayer().getName(), failure);
            return null;
        });
    }

    /**
     * Saves and evicts a leaving player's data.
     *
     * @param event The quit event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        store.unload(DataScopes.of(event.getPlayer()));
    }

    /**
     * Saves and evicts a chunk's data.
     * <p>
     * There is deliberately no matching load handler. Preloading on chunk load would mean a disk
     * lookup for every chunk a player walks past, the overwhelming majority of which will never have
     * any Ascendancy data attached — so chunk data is instead read on first access.
     *
     * @param event The chunk unload event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkUnload(ChunkUnloadEvent event) {
        store.getIfLoaded(DataScopes.of(event.getChunk())).ifPresent(holder -> store.unload(holder.getKey()));
    }
}
