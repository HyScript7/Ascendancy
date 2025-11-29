package io.github.hyscript7.ascendancy.features.voidrealm.listeners;

import io.github.hyscript7.ascendancy.AscendancyPlugin;
import io.github.hyscript7.ascendancy.data.players.PlayerData;
import io.github.hyscript7.ascendancy.data.players.PlayerDataManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * Handles loss of lives upon dying
 */
public class PlayerDeathListener implements Listener {
    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        // "You won't use the shorthand" they said. Oh, yeah? What's this then?
        PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(player);

        if (playerData == null) {
            // The fuck?
            return;
        }

        if (player.getGameMode() == GameMode.CREATIVE ||
                player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        // But for real, how does this know to look for a player and not a mob?
        Player killer = player.getKiller();

        boolean isPvPDeath = killer != null;
        boolean isSuicide = killer != null && killer.equals(player);
        // Natural death causes, like fall damage, don't count as PvE.
        boolean isPvEDeath = killer == null && isDamagedByEntity(event);

        boolean isPvEEnabled = AscendancyPlugin.getInstance().getConfig().getBoolean("void_ban.pve_enabled", false);

        boolean shouldLoseLife = ((isPvEDeath && isPvEEnabled)|| (isPvPDeath && !isSuicide));

        if (!shouldLoseLife) {
            return;
        }

        if (isPvPDeath) {
            playerData.incrementPvpDeaths();
            PlayerDataManager.getInstance().getPlayerData(killer).incrementPvpKills();
        } else if (isPvEDeath) {
            playerData.incrementPveDeaths();
        }

        int previousLives = playerData.getLives();
        playerData.removeLives(1); // Potential for relics which take multiple lives?
        int currentLives = playerData.getLives();

        if (currentLives > 0) {
            // Still alive
            // TODO: Format & change
            player.sendMessage("You have died, blah blah blah... you have " + currentLives + " lives left bitchass.");
        } else {
            if (!playerData.isDead()) {
                // Fucking dead
                // TODO: Format & change
                player.sendMessage("You have lost all your lives and will respawn in the void realm.");
                // TODO: Announce the void ban to the server
                Bukkit.getServer().broadcast(player.displayName().append(Component.text(" has been void banned. Git gut.")));
                // TODO: Handle void death flags & animations
                playerData.setDead(true);
            }
        }
    }

    /**
     * Checks whether the cause of death is by a living non-player entity.
     * <p>
     * Will return false for natural deaths like fall damage.
     * @param event The player death event
     * @return True if the event was caused by a non-player mob, otherwise false.
     */
    private boolean isDamagedByEntity(PlayerDeathEvent event) {
        Entity killer = event.getEntity().getKiller();
        if (killer != null) return true;

        EntityDamageEvent lastDamage = event.getEntity().getLastDamageCause();

        return lastDamage instanceof EntityDamageByEntityEvent;
    }

    /**
     * Similar to isDamagedByEntity, but instead of a boolean, it returns either the attacker or null.
     * @param event The player death event
     * @return An entity if killed by a mob, otherwise null for natural and PvP causes
     */
    private Entity getKillerMob(PlayerDeathEvent event) {
        Entity killer = event.getEntity().getKiller();
        if (killer != null) return null;

        EntityDamageEvent lastDamage = event.getEntity().getLastDamageCause();

        if (lastDamage instanceof EntityDamageByEntityEvent damageEvent) {
            return damageEvent.getDamager();
        }
        return null;
    }
}
