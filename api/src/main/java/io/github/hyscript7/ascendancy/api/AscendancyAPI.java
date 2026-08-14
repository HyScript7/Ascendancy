package io.github.hyscript7.ascendancy.api;

import io.github.hyscript7.ascendancy.api.data.Persistence;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;

public interface AscendancyAPI {
    /**
     * The persistence layer, used to attach arbitrary data to players, chunks, factions, or anything
     * else a feature decides to store data on.
     *
     * @return The persistence entry point
     */
    Persistence persistence();

    /**
     * A holder for the API instance
     */
    class Holder {
        private static AscendancyAPI INSTANCE;

        private Holder() {}
    }

    static AscendancyAPI get() {
        if (Holder.INSTANCE == null) {
            throw new IllegalStateException(
                    "AscendancyAPI instance is not set. Ensure your content pack loads AFTER the Core has registered itself with the API.");
        }
        return Holder.INSTANCE;
    }

    static Optional<AscendancyAPI> fromServicesManager() {
        return Optional.ofNullable(Bukkit.getServer().getServicesManager().load(AscendancyAPI.class));
    }

    static void set(AscendancyAPI instance, Plugin plugin) throws AscendancyAPIAlreadyInitializedException {
        if (Holder.INSTANCE != null) {
            throw new AscendancyAPIAlreadyInitializedException(
                    "AscendancyAPI instance is already set. Cannot set it again.");
        }

        Holder.INSTANCE = instance;
        Bukkit.getServicesManager().register(AscendancyAPI.class, Holder.INSTANCE, plugin, ServicePriority.Normal);
    }
}
