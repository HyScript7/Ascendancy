package io.github.hyscript7.ascendancy.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class AscendancyEvent extends Event {
    public AscendancyEvent() {
        super();
    }

    public AscendancyEvent(boolean isAsync) {
        super(isAsync);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return new HandlerList();
    }

    private static final HandlerList handlerList = new HandlerList();
}
