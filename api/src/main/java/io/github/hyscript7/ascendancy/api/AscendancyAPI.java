package io.github.hyscript7.ascendancy.api;

import io.github.hyscript7.ascendancy.api.data.Persistence;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;

/**
 * The entry point to everything Ascendancy Core provides.
 * <p>
 * The instance lives in Bukkit's services manager, which is deliberately the single source of truth
 * — there is no static cache here to go stale across a reload.
 * <p>
 * <strong>Content packs should resolve the instance once and hold it</strong>, rather than calling
 * {@link #get()} wherever they happen to need it. Every call is a services manager lookup, and that
 * lookup takes a lock; doing it per entity per tick is a needless cost on the main thread. Resolve it
 * when Core announces itself and drop it when the pack shuts down, so the reference cannot outlive
 * the Core that produced it:
 *
 * <pre>{@code
 * public final class MyPack extends JavaPlugin implements Listener {
 *     private AscendancyAPI ascendancy;
 *
 *     @Override
 *     public void onEnable() {
 *         getServer().getPluginManager().registerEvents(this, this);
 *     }
 *
 *     @EventHandler
 *     public void onAscendancyEnabled(AscendancyEnabledEvent event) {
 *         ascendancy = AscendancyAPI.get();
 *         // register component types and scopes here
 *     }
 *
 *     @EventHandler
 *     public void onAscendancyDisabled(AscendancyDisabledEvent event) {
 *         ascendancy = null; // Core is going away; do not outlive it
 *     }
 * }
 * }</pre>
 */
public interface AscendancyAPI {
    /**
     * The persistence layer, used to attach arbitrary data to players, chunks, factions, or anything
     * else a feature decides to store data on.
     *
     * @return The persistence entry point
     */
    Persistence persistence();

    /**
     * Retrieves the AscendancyAPI instance from Bukkit's services manager.
     * Unwraps the optional and throws if uninitialized.
     * <p>
     * Not free: this is a services manager lookup, and that lookup takes a lock. Resolve it once and
     * hold the reference rather than calling this on a hot path — see the class documentation for the
     * pattern.
     *
     * @return The AscendancyAPI instance
     * @throws IllegalStateException If not initialized
     */
    static AscendancyAPI get() {
        Optional<AscendancyAPI> instance = fromServicesManager();
        if (instance.isEmpty()) {
            throw new IllegalStateException(
                    "AscendancyAPI instance is not set. Ensure your content pack loads AFTER the Core has registered itself with the API.");
        }
        return instance.get();
    }

    /**
     * Retrieves the AscendancyAPI instance from Bukkit's services manager and wraps
     * it in an optional.
     *
     * @return An optional containing the API if it is initialized, otherwise an
     *         empty optional.
     */
    static Optional<AscendancyAPI> fromServicesManager() {
        return Optional.ofNullable(Bukkit.getServer().getServicesManager().load(AscendancyAPI.class));
    }

    /**
     * A shortcut for registering the API instance with Bukkit's services manager.
     *
     * @param instance The AscendancyAPI instance
     * @param plugin   The plugin providing the Core implementation.
     * @throws AscendancyAPIAlreadyInitializedException If already registered.
     */
    static void set(AscendancyAPI instance, Plugin plugin) throws AscendancyAPIAlreadyInitializedException {
        if (fromServicesManager().isPresent()) {
            throw new AscendancyAPIAlreadyInitializedException(
                    "AscendancyAPI instance is already set. Cannot set it again.");
        }
        Bukkit.getServicesManager().register(AscendancyAPI.class, instance, plugin, ServicePriority.Normal);
    }
}
