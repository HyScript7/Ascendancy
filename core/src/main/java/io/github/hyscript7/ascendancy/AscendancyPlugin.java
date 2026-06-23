package io.github.hyscript7.ascendancy;

import io.github.hyscript7.ascendancy.api.AscendancyAPI;
import io.github.hyscript7.ascendancy.api.events.AscendancyDisabledEvent;
import io.github.hyscript7.ascendancy.api.events.AscendancyEnabledEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;

public class AscendancyPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        // All remaining code goes above enable event, event is called when plugin is completely ready
        AscendancyAPI api = new AscendancyAPI(){

        };
        AscendancyAPI.set(api, this);
        Bukkit.getPluginManager().callEvent(new AscendancyEnabledEvent(api));
    }

    @Override
    public void onDisable() {
        Bukkit.getPluginManager().callEvent(new AscendancyDisabledEvent());
        // All remaining code goes under disable event, event is called when plugin starts shutting down
    }
}
