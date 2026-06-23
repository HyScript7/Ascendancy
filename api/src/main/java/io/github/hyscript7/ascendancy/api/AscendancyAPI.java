package io.github.hyscript7.ascendancy.api;

import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import java.util.Optional;
import org.bukkit.plugin.Plugin;

public interface AscendancyAPI {
    /**
     * A holder for the API instance
     */
    class Holder {
        private static AscendancyAPI INSTANCE;

        private Holder() {
        }
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