package io.github.hyscript7.ascendancy.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import io.github.hyscript7.ascendancy.api.AscendancyAPI;

public class AscendancyEnabledEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final AscendancyAPI api;

    public AscendancyEnabledEvent(AscendancyAPI api) {
        this.api = api;
    }

    public AscendancyAPI getApi() {
        return api;
    }
    

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}