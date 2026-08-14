package io.github.hyscript7.ascendancy.api.event;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when Ascendancy Core is disabling.
 *
 * <p>
 * Content packs should listen to this event and release their reference to AscendancyAPI and other resources.
 * </p>
 */
public class AscendancyDisabledEvent extends AscendancyEvent {
    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    private static final HandlerList handlerList = new HandlerList();
}
