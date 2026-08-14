package io.github.hyscript7.ascendancy.api.event;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class AscendancyDisabledEvent extends AscendancyEvent {
    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return new HandlerList();
    }

    private static final HandlerList handlerList = new HandlerList();
}
