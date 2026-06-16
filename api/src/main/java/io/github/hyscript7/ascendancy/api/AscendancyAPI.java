package io.github.hyscript7.ascendancy.api;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import java.util.Optional;
import org.bukkit.plugin.Plugin;

public interface class AscendancyAPI {
    class Holder {
        private static final AscendancyAPI INSTANCE;
    }

    static AscendancyAPI get(){
        if (Holder.INSTANCE == null) {
            throw new IllegalStateException("AscendancyAPI instance is not set. Please set it using AscendancyAPI.set() before calling AscendancyAPI.get().");
        }
        return Holder.INSTANCE;
    }

    static Optional<AscendancyAPI> fromServicesManager(){
        if (Optional.of(Holder.INSTANCE).isPresent()) {
            return Optional.of(Holder.INSTANCE);
        }
        return Optional.empty()
    }

    static void set(AscendancyAPI instance, Plugin plugin){
        if (Holder.INSTANCE != null) {
            throw new AscendancyAPIAlreadyInitializedException("AscendancyAPI instance is already set. Cannot set it again.");
        }
        
        Holder.INSTANCE = instance;
        Bukkit.getServicesManager().register(AscendancyAPI.class, Holder.INSTANCE, plugin, ServicePriority.Normal);
    }
}