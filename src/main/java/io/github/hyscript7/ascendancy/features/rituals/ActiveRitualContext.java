package io.github.hyscript7.ascendancy.features.rituals;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public record ActiveRitualContext(Player player, Location location, Ritual ritual, BukkitTask task) {
    public void cancel() {
        task.cancel();
    }
}
