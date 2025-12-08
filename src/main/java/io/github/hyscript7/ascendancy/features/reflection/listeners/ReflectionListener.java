package io.github.hyscript7.ascendancy.features.reflection.listeners;

import io.github.hyscript7.ascendancy.AscendancyMessagingAPI;
import io.github.hyscript7.ascendancy.AscendancyPlugin;
import io.github.hyscript7.ascendancy.data.players.PlayerData;
import io.github.hyscript7.ascendancy.data.players.PlayerDataManager;
import io.github.hyscript7.ascendancy.features.reflection.ReflectionUtility;
import io.github.hyscript7.ascendancy.features.reflection.citizens.BossSpellCastBehavior;
import io.github.hyscript7.ascendancy.features.reflection.citizens.ReflectionTrait;
import io.github.hyscript7.ascendancy.features.voidrealm.VoidRealmLayer;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.mcmonkey.sentinel.SentinelTrait;

import java.util.UUID;

public class ReflectionListener implements Listener {

    private static final double REFLECTION_HEALTH_MULTIPLIER = 10.0;
    private static final double REFLECTION_CHASE_RANGE = 500.0;

    @EventHandler
    public void playerDescendsIntoReflectionOfSelf(PlayerTeleportEvent event) {
        // Skip if NPC or not entering Reflection layer
        // For your information, if we don't check for NPC, we might end up creating an infinite amount of NPCs recursively
        if (ReflectionUtility.isNPC(event.getPlayer())) return;
        if (event.getFrom().getWorld().equals(VoidRealmLayer.REFLECTION.getWorld())) return;
        if (!event.getTo().getWorld().equals(VoidRealmLayer.REFLECTION.getWorld())) return;

        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(player);

        // TODO: Once levels are implemented, check if the player is Lv 50

        // Check if reflection already exists
        if (playerData.hasReflection()) {
            UUID reflectionUuid = playerData.getReflectionUuid();
            NPC existingNpc = ReflectionUtility.getNPCByUUID(reflectionUuid);
            if (existingNpc != null) return; // Reflection still exists, don't spawn new one
        }

        // Spawn reflection
        double offsetX = (Math.random() - 0.5) * 32;
        double offsetZ = (Math.random() - 0.5) * 32;
        Location spawnLocation = VoidRealmLayer.REFLECTION.getWorld().getSpawnLocation();
        spawnLocation.setY(3);
        spawnLocation.setX(spawnLocation.getX() + offsetX);
        spawnLocation.setZ(spawnLocation.getZ() + offsetZ);

        UUID reflectionUuid = spawnPlayerReflection(player, spawnLocation);
        playerData.setReflectionUuid(reflectionUuid);

        // Notify player
        AscendancyMessagingAPI.getInstance().sendBoxed(
                player,
                AscendancyMessagingAPI.MessageType.INFO,
                "Resurrection",
                null,
                "You have descended to the 3rd layer of the Void Realm: <bold>Reflection of Self</bold>\n\nSomeone is waiting for you..."
        );

        AscendancyPlugin.getInstance().getLogger().info(
                "Spawning reflection for player " + player.getName() +
                        " (" + player.getUniqueId() + ") with NPC uuid " + reflectionUuid
        );
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCloneDamagedBySomeoneElse(EntityDamageByEntityEvent event) {
        // Only care about player victims and player attackers
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!ReflectionUtility.isNPC(victim)) return;

        NPC npc = ReflectionUtility.getNPC(victim);
        if (!ReflectionUtility.hasTrait(npc, ReflectionTrait.class)) return;

        ReflectionTrait reflectionTrait = ReflectionUtility.getTrait(npc, ReflectionTrait.class);

        // If attacked by owner, that's allowed
        if (reflectionTrait.getOwnerUuid().equals(attacker.getUniqueId())) return;

        // Otherwise, reflect damage back to the owner
        Player owner = Bukkit.getPlayer(reflectionTrait.getOwnerUuid());
        if (owner == null) return; // Owner offline, skip

        // Punish the owner for getting help
        AscendancyPlugin.getInstance().getLogger().info(
                "Reflection owned by " + owner.getName() +
                        " has been damaged by " + attacker.getName() + ", reflecting to owner."
        );

        owner.damage(
                4,
                DamageSource.builder(DamageType.PLAYER_ATTACK)
                        .withDamageLocation(victim.getLocation())
                        .withCausingEntity(victim)
                        .withDirectEntity(attacker)
                        .build()
        );

        AscendancyMessagingAPI.getInstance().actionBar(
                owner,
                "<red>Your soul is in pain... someone is attacking your <bold>reflection</bold>."
        );
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCloneDie(PlayerDeathEvent event) {
        if (!ReflectionUtility.isNPC(event.getPlayer())) return;

        NPC npc = ReflectionUtility.getNPC(event.getPlayer());
        if (!ReflectionUtility.hasTrait(npc, ReflectionTrait.class)) return;

        // It's a reflection, handle death
        ReflectionTrait reflectionTrait = ReflectionUtility.getTrait(npc, ReflectionTrait.class);
        UUID ownerUuid = reflectionTrait.getOwnerUuid();
        Player owner = Bukkit.getPlayer(ownerUuid);

        if (owner != null) {
            PlayerData data = PlayerDataManager.getInstance().getPlayerData(owner);
            if (data != null) {
                data.setReflectionUuid(null);

                // Check if owner got the kill
                if (owner.equals(event.getPlayer().getKiller())) {
                    handleReflectionKilledByOwner(owner, event.getPlayer());
                } else {
                    handleReflectionKilledByOther(owner, event.getPlayer());
                }
            }
        }

        npc.destroy();
    }

    private void handleReflectionKilledByOwner(Player owner, Player reflectionEntity) {
        PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(owner);

        String newResurrection = "N/A";

        if (playerData != null) {
            playerData.incrementResurrection(1);
            newResurrection = String.valueOf(playerData.getResurrection());
            playerData.setLives(playerData.getMaxLives());
        }

        AscendancyMessagingAPI.getInstance().sendBoxed(
                owner,
                AscendancyMessagingAPI.MessageType.INFO,
                "Resurrection",
                null,
                "You have killed your <bold>Reflection of Self</bold>\n\nYou are now at Resurrection <bold><white>" + newResurrection + "</white></bold>"
        );

        reflectionEntity.getLocation().getWorld().playSound(
                reflectionEntity,
                Sound.ENTITY_ENDER_DRAGON_DEATH,
                0.5f,
                0.25f
        );

        AscendancyPlugin.getInstance().getLogger().info(
                "Reflection owned by " + owner.getName() + " has been killed!"
        );
    }

    private void handleReflectionKilledByOther(Player owner, Player reflectionEntity) {
        AscendancyMessagingAPI.getInstance().sendBoxed(
                owner,
                AscendancyMessagingAPI.MessageType.ERROR,
                "Resurrection",
                null,
                "Someone else has killed your <bold>Reflection</bold>. You have not been given kill credit."
        );

        reflectionEntity.getLocation().getWorld().playSound(
                reflectionEntity,
                Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN,
                0.5f,
                0.25f
        );

        AscendancyPlugin.getInstance().getLogger().info(
                "Reflection owned by " + owner.getName() +
                        " has been killed by " + (reflectionEntity.getKiller() != null ? reflectionEntity.getKiller().getName() : "an unknown entity") +
                        ", not giving kill credit."
        );
    }

    private UUID spawnPlayerReflection(Player player, Location spawnLocation) {
        // Create the reflection using the utility
        UUID npcUuid = ReflectionUtility.createPlayerClone(
                player,
                spawnLocation,
                REFLECTION_HEALTH_MULTIPLIER,
                REFLECTION_CHASE_RANGE
        );

        // Get the NPC to add custom traits
        NPC npc = ReflectionUtility.getNPCByUUID(npcUuid);
        if (npc == null) {
            throw new IllegalStateException("Failed to create reflection NPC");
        }

        // Add the reflection trait to mark this as a reflection
        npc.getOrAddTrait(ReflectionTrait.class).setOwnerUuid(player.getUniqueId());

        // Add spell casting behavior
        SentinelTrait sentinel = ReflectionUtility.getTrait(npc, SentinelTrait.class);
        if (sentinel != null) {
            ReflectionUtility.addBehavior(
                    npc,
                    new BossSpellCastBehavior(npc, sentinel.chaseRange),
                    1
            );
        }

        return npcUuid;
    }
}