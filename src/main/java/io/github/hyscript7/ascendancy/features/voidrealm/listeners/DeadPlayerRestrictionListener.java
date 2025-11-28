package io.github.hyscript7.ascendancy.features.voidrealm.listeners;

import io.github.hyscript7.ascendancy.data.players.PlayerData;
import io.github.hyscript7.ascendancy.data.players.PlayerDataManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class DeadPlayerRestrictionListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlacingBlocksWhileDead(BlockPlaceEvent event) {
        PlayerData data = PlayerDataManager.getInstance().getPlayerData(event.getPlayer());
        if (shouldRestrict(event.getPlayer(), data)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBreakingBlocksWhileDead(BlockBreakEvent event) {
        PlayerData data = PlayerDataManager.getInstance().getPlayerData(event.getPlayer());
        if (shouldRestrict(event.getPlayer(), data)) {
            event.setCancelled(true);
        }
    }

    /**
     * Checks whether the player should have various void ban imposed restrictions, such as mining or placing
     * blocks enforced.
     * @param player The player to test
     * @return true if the player has bypass, otherwise false.
     */
    private boolean shouldRestrict(Player player, PlayerData playerData) {
        return playerData.isDead() && (!player.getGameMode().equals(GameMode.CREATIVE) && !player.getGameMode().equals(GameMode.SPECTATOR));
    }

}
