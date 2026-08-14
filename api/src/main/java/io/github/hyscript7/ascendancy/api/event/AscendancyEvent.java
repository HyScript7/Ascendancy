package io.github.hyscript7.ascendancy.api.event;

import org.bukkit.event.Event;

/**
 * A base Ascendancy Event.
 *
 * <p>
 * If you are inheriting this class to create an event, you must include these methods and static variable as per PaperMC documentation:
 * <pre>{@code
 * @Override
 * public @NotNull HandlerList getHandlers() {
 *     return handlerList;
 * }
 *
 * public static HandlerList getHandlerList() {
 *    return handlerList;
 * }
 *
 * private static final HandlerList handlerList = new HandlerList();
 * }</pre>
 */
public abstract class AscendancyEvent extends Event {
    protected AscendancyEvent() {
        super();
    }

    protected AscendancyEvent(boolean isAsync) {
        super(isAsync);
    }
}
