package io.github.hyscript7.ascendancy.features.voidrealm.listeners;

import io.github.hyscript7.ascendancy.AscendancyConfig;
import io.github.hyscript7.ascendancy.data.players.PlayerData;
import io.github.hyscript7.ascendancy.data.players.PlayerDataManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class DeadPlayerRestrictionListener implements Listener {

    private final boolean preventPlacing;
    private final boolean preventBreaking;
    private final boolean preventInteracting;

    public DeadPlayerRestrictionListener() {
        AscendancyConfig.VoidRealm.DeadPlayerRestrictions restrictionConfig = AscendancyConfig.getInstance().getVoidRealm().deadPlayerRestrictions();
        preventPlacing = restrictionConfig.noBuild();
        preventBreaking = restrictionConfig.noBreak();
        preventInteracting = restrictionConfig.noInteract();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlacingBlocksWhileDead(BlockPlaceEvent event) {
        if (!preventPlacing) return;
        PlayerData data = PlayerDataManager.getInstance().getPlayerData(event.getPlayer());
        if (shouldRestrict(event.getPlayer(), data)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBreakingBlocksWhileDead(BlockBreakEvent event) {
        if (!preventBreaking) return;
        PlayerData data = PlayerDataManager.getInstance().getPlayerData(event.getPlayer());
        if (shouldRestrict(event.getPlayer(), data)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteractingBlocksWhileDead(PlayerInteractEvent event) {
        if (!preventInteracting) return;
        if (shouldRestrict(event.getPlayer(), PlayerDataManager.getInstance().getPlayerData(event.getPlayer()))) {
            if (event.getClickedBlock() != null) {
                event.setCancelled(true);
            }
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
