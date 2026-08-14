package io.github.hyscript7.ascendancy.packs;

import io.github.hyscript7.ascendancy.api.AscendancyAPI;
import io.github.hyscript7.ascendancy.api.data.component.ComponentType;
import io.github.hyscript7.ascendancy.api.data.store.DataScopes;
import io.github.hyscript7.ascendancy.api.event.AscendancyDisabledEvent;
import io.github.hyscript7.ascendancy.api.event.AscendancyEnabledEvent;
import io.github.hyscript7.ascendancy.api.registry.Identifier;
import io.github.hyscript7.ascendancy.packs.example.ExampleVisitLog;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The built-in content pack, currently carrying one worked example of the Core handshake and the
 * persistence API.
 * <p>
 * The whole flow lives in this one class on purpose — a reference you can read top to bottom before
 * writing a pack of your own. Delete the example bits once there is real content here.
 */
@Slf4j
public class AscendancyBuiltinPack extends JavaPlugin implements Listener {
    /**
     * Held from {@link AscendancyEnabledEvent} until {@link AscendancyDisabledEvent}, rather than
     * calling {@code AscendancyAPI.get()} at each use — that is a services manager lookup, and it
     * takes a lock. Null whenever Core is not up, which is why every use below checks first.
     */
    private AscendancyAPI ascendancy;

    /**
     * Built during registration rather than as a constant, because the identifier's namespace comes
     * from this plugin's name and there is no plugin instance until Bukkit makes one.
     */
    private ComponentType<ExampleVisitLog> visitLog;

    /**
     * Registers listeners and nothing else.
     * <p>
     * Core may not be enabled yet, so touching the API here would be a coin flip. All real work waits
     * for {@link #onAscendancyEnabled(AscendancyEnabledEvent)}.
     */
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    /**
     * Core is up and the API is usable. Register component types and any scopes of your own here.
     * <p>
     * The event carries the instance, so a pack does not even need a lookup to get started.
     *
     * @param event Core announcing itself
     */
    @EventHandler
    public void onAscendancyEnabled(AscendancyEnabledEvent event) {
        ascendancy = event.getApi();

        // Identifier.of(plugin, path) so the namespace is this plugin's name — that is what keeps two
        // packs from colliding on "visit_log".
        visitLog = ExampleVisitLog.componentType(Identifier.of(this, "visit_log"));

        // Registration is mandatory: it is what stops two packs claiming one identifier and quietly
        // overwriting each other. set() refuses an unregistered type.
        ascendancy.persistence().componentTypes().register(visitLog);

        log.info("Registered example component {}", visitLog.getIdentifier());
    }

    /**
     * Core is going away. Drop the references so nothing here outlives it.
     *
     * @param event Core announcing its shutdown
     */
    @EventHandler
    public void onAscendancyDisabled(AscendancyDisabledEvent event) {
        ascendancy = null;
        visitLog = null;
    }

    /**
     * Reads, updates and writes a component — the entire point of the example.
     * <p>
     * Note what is absent: no save call, no null handling for a first-time player, no futures. The
     * default value covers the new player, {@code set} marks the entity dirty, and Core writes it out
     * on its own schedule and again at shutdown.
     *
     * @param event The joining player
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (ascendancy == null) {
            // Core is down; a pack that assumes otherwise is a pack that throws during shutdown.
            return;
        }

        var holder = ascendancy.persistence().store().get(DataScopes.of(event.getPlayer()));
        ExampleVisitLog previous = holder.get(visitLog);
        ExampleVisitLog updated = previous.withVisitAt(System.currentTimeMillis());
        holder.set(visitLog, updated);

        event.getPlayer()
                .sendMessage(Component.text(
                        previous.returning()
                                ? "Welcome back. This is visit number " + updated.visits() + "."
                                : "First time here. The Abyss is taking notes."));
    }
}
