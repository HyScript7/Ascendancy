package io.github.hyscript7.ascendancy.api.event;

import io.github.hyscript7.ascendancy.api.AscendancyAPI;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when AscendancyCore is finished loading and the API is ready.
 *
 * <p>
 * An instance of the API is provided alongside this event and can be accessed using a getter:
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
 *         ascendancy = event.getApi();
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
public class AscendancyEnabledEvent extends AscendancyEvent {
    private final AscendancyAPI api;

    public AscendancyEnabledEvent(AscendancyAPI api) {
        super();
        this.api = api;
    }

    /**
     * @return The AscendancyAPI instance
     */
    public AscendancyAPI getApi() {
        return api;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    private static final HandlerList handlerList = new HandlerList();
}
