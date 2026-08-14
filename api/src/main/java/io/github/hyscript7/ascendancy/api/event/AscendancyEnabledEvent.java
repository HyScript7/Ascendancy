package io.github.hyscript7.ascendancy.api.event;

import io.github.hyscript7.ascendancy.api.AscendancyAPI;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class AscendancyEnabledEvent extends AscendancyEvent {
    private final AscendancyAPI api;

    public AscendancyEnabledEvent(AscendancyAPI api) {
        super();
        this.api = api;
    }

    public AscendancyAPI getApi() {
        return api;
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
