package io.github.hyscript7.ascendancy.api.event;

import org.bukkit.event.Event;

public abstract class AscendancyEvent extends Event {
    protected AscendancyEvent() {
        super();
    }

    protected AscendancyEvent(boolean isAsync) {
        super(isAsync);
    }
}
