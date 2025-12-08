package io.github.hyscript7.ascendancy.features.reflection;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.tree.BehaviorGoalAdapter;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.trait.SkinTrait;
import net.citizensnpcs.trait.versioned.BossBarTrait;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.mcmonkey.sentinel.SentinelTrait;

import java.util.UUID;

/**
 * Utility class for creating and managing player reflection NPCs
 */
public class ReflectionUtility {

    /**
     * Creates a player clone NPC with full equipment and combat capabilities
     *
     * @param player The player to clone
     * @param spawnLocation Where to spawn the clone
     * @param healthMultiplier Multiplier for the clone's health (e.g., 10.0 for 10x health)
     * @param chaseRange How far the NPC will chase targets
     * @return UUID of the created NPC
     */
    public static UUID createPlayerClone(Player player, Location spawnLocation, double healthMultiplier, double chaseRange) {
        NPC npc = createBasePlayerNPC(player, player.getName() + "'s Clone", spawnLocation);

        copyPlayerEquipment(npc, player);
        setupBossBar(npc, player.getName() + "'s Reflection", BarColor.WHITE, 500);

        double maxHealth = getPlayerMaxHealth(player) * healthMultiplier;
        configureSentinel(npc, player, maxHealth, chaseRange, spawnLocation);

        updateBossBarHealthProvider(npc, maxHealth);

        return npc.getUniqueId();
    }

    /**
     * Creates a basic player NPC with skin and nameplate settings
     *
     * @param player The player whose appearance to copy
     * @param npcName The name for the NPC
     * @param location Where to spawn the NPC
     * @return The created NPC
     */
    public static NPC createBasePlayerNPC(Player player, String npcName, Location location) {
        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, npcName);
        npc.data().setPersistent(NPC.Metadata.NAMEPLATE_VISIBLE, false);
        npc.getOrAddTrait(SkinTrait.class).setSkinPersistent(player);
        npc.spawn(location);
        return npc;
    }

    /**
     * Copies all equipment from a player to an NPC
     *
     * @param npc The NPC to equip
     * @param player The player to copy equipment from
     */
    public static void copyPlayerEquipment(NPC npc, Player player) {
        Equipment equipment = npc.getOrAddTrait(Equipment.class);

        copyArmorPiece(equipment, Equipment.EquipmentSlot.HELMET, player.getInventory().getHelmet());
        copyArmorPiece(equipment, Equipment.EquipmentSlot.CHESTPLATE, player.getInventory().getChestplate());
        copyArmorPiece(equipment, Equipment.EquipmentSlot.LEGGINGS, player.getInventory().getLeggings());
        copyArmorPiece(equipment, Equipment.EquipmentSlot.BOOTS, player.getInventory().getBoots());
        copyArmorPiece(equipment, Equipment.EquipmentSlot.HAND, player.getInventory().getItemInMainHand());
        copyArmorPiece(equipment, Equipment.EquipmentSlot.OFF_HAND, player.getInventory().getItemInOffHand());
    }

    /**
     * Copies a single armor/equipment piece to an NPC
     *
     * @param equipment The Equipment trait
     * @param slot The equipment slot
     * @param item The item to copy (can be null)
     */
    private static void copyArmorPiece(Equipment equipment, Equipment.EquipmentSlot slot, ItemStack item) {
        if (item != null) {
            equipment.set(slot, item.clone());
        }
    }

    /**
     * Sets up a boss bar for an NPC
     *
     * @param npc The NPC to add the boss bar to
     * @param title The title of the boss bar
     * @param color The color of the boss bar
     * @param range The range at which the boss bar is visible
     */
    public static void setupBossBar(NPC npc, String title, BarColor color, double range) {
        BossBarTrait bossbar = npc.getOrAddTrait(BossBarTrait.class);
        bossbar.setColor(color);
        bossbar.setTitle(title);
        bossbar.setRange((int) range);
    }

    /**
     * Updates the boss bar to track the NPC's health
     *
     * @param npc The NPC with the boss bar
     * @param maxHealth The maximum health for percentage calculation
     */
    public static void updateBossBarHealthProvider(NPC npc, double maxHealth) {
        BossBarTrait bossbar = npc.getTraitNullable(BossBarTrait.class);
        if (bossbar != null && npc.getEntity() instanceof LivingEntity) {
            bossbar.setProgressProvider(() -> ((LivingEntity) npc.getEntity()).getHealth() / maxHealth);
        }
    }

    /**
     * Configures Sentinel trait for combat behavior
     *
     * @param npc The NPC to configure
     * @param targetPlayer The primary target player
     * @param maxHealth The maximum health to set
     * @param chaseRange How far the NPC will chase targets
     * @param spawnPoint The spawn point for respawning
     */
    public static void configureSentinel(NPC npc, Player targetPlayer, double maxHealth, double chaseRange, Location spawnPoint) {
        SentinelTrait sentinel = npc.getOrAddTrait(SentinelTrait.class);

        sentinel.addTarget("player:" + targetPlayer.getName());
        sentinel.addTarget("players");
        sentinel.removeIgnore("owner");

        sentinel.chaseRange = chaseRange;
        sentinel.autoswitch = true;
        sentinel.closeChase = true;
        sentinel.spawnPoint = spawnPoint;
        sentinel.respawnTime = -1;

        sentinel.setHealth(maxHealth);
        sentinel.health = maxHealth;
    }

    /**
     * Gets a player's maximum health
     *
     * @param player The player
     * @return The player's max health, or 20.0 if unable to determine
     */
    public static double getPlayerMaxHealth(Player player) {
        AttributeInstance maxHealthAttribute = player.getAttribute(Attribute.MAX_HEALTH);
        return maxHealthAttribute != null ? maxHealthAttribute.getValue() : 20.0;
    }

    /**
     * Checks if an entity is a Citizens NPC
     *
     * @param player The player entity to check
     * @return true if the player is an NPC
     */
    public static boolean isNPC(Player player) {
        return CitizensAPI.getNPCRegistry().isNPC(player);
    }

    /**
     * Gets an NPC by its UUID
     *
     * @param uuid The UUID of the NPC
     * @return The NPC, or null if not found
     */
    public static NPC getNPCByUUID(UUID uuid) {
        return CitizensAPI.getNPCRegistry().getByUniqueId(uuid);
    }

    /**
     * Gets the NPC from a player entity (if it is one)
     *
     * @param player The player entity
     * @return The NPC, or null if the player is not an NPC
     */
    public static NPC getNPC(Player player) {
        if (!isNPC(player)) {
            return null;
        }
        return CitizensAPI.getNPCRegistry().getNPC(player);
    }

    /**
     * Checks if an NPC has a specific trait
     *
     * @param npc The NPC to check
     * @param traitClass The trait class to check for
     * @return true if the NPC has the trait
     */
    public static <T extends Trait> boolean hasTrait(NPC npc, Class<T> traitClass) {
        return npc.getTraitOptional(traitClass).isPresent();
    }

    /**
     * Safely gets a trait from an NPC
     *
     * @param npc The NPC
     * @param traitClass The trait class
     * @return The trait, or null if not present
     */
    public static <T extends Trait> @Nullable T getTrait(NPC npc, Class<T> traitClass) {
        return npc.getTraitNullable(traitClass);
    }

    /**
     * Adds a custom behavior to an NPC's goal controller
     *
     * @param npc The NPC
     * @param behavior The behavior to add
     * @param priority The priority (lower = higher priority)
     */
    public static <T extends BehaviorGoalAdapter> void addBehavior(NPC npc, T behavior, int priority) {
        npc.getDefaultGoalController().addBehavior(behavior, priority);
    }
}