package io.github.hyscript7.ascendancy.builtins.rituals.instant;

import io.github.hyscript7.ascendancy.AscendancyMessagingAPI;
import io.github.hyscript7.ascendancy.AscendancyPlugin;
import io.github.hyscript7.ascendancy.data.players.PlayerData;
import io.github.hyscript7.ascendancy.data.players.PlayerDataManager;
import io.github.hyscript7.ascendancy.features.rituals.*;
import io.github.hyscript7.ascendancy.features.rituals.requirements.ItemExclusivityFactory;
import io.github.hyscript7.ascendancy.features.rituals.requirements.ItemSacrifice;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class RitualOfRevival extends AbstractRitual {
    private static final double ritualRange = 8.0d;

    public RitualOfRevival() {
        super("ritual revival temporary", "Revival", RitualGrade.MYTHIC, buildStages());
    }

    private static final Set<Material> primary = Set.of(
            Material.TOTEM_OF_UNDYING
    );

    private static final Set<Material> supplementarySecond = Set.of(
            Material.ENCHANTED_GOLDEN_APPLE, Material.WITHER_SKELETON_SKULL
    );

    private static final Set<Material> supplementaryThird = Set.of(
            Material.SOUL_LANTERN, Material.ECHO_SHARD, Material.AMETHYST_SHARD
    );

    private static Set<Material> getAllMaterials() {
        Set<Material> materials = new HashSet<>();
        materials.addAll(primary);
        materials.addAll(supplementarySecond);
        materials.addAll(supplementaryThird);
        return materials;
    }

    private static final ItemExclusivityFactory exclusivity = new ItemExclusivityFactory(getAllMaterials());

    private static List<RitualStage> buildStages() {
        return List.of(exclusivity.on(
                        ItemSacrifice.builder().itemType(Material.TOTEM_OF_UNDYING).minAmount(1).build()
                ),
                exclusivity.on(
                        ItemSacrifice.builder().itemType(Material.ENCHANTED_GOLDEN_APPLE).minAmount(1).fallback(
                                ItemSacrifice.builder().itemType(Material.WITHER_SKELETON_SKULL).minAmount(1).build()
                        ).build()
                ),
                exclusivity.on(
                        ItemSacrifice.builder().itemType(Material.SOUL_LANTERN).minAmount(1).fallback(
                                ItemSacrifice.builder().itemType(Material.ECHO_SHARD).minAmount(1).fallback(
                                        ItemSacrifice.builder().itemType(Material.AMETHYST_SHARD).minAmount(1).build()
                                ).build()
                        ).build()
                ));
    }

    private int countItemsOfCategory(RitualContext context, Set<Material> materials) {
        return context.getSacrificedItems().stream()
                .filter(itemStack -> !materials.contains(itemStack.getType()))
                .mapToInt(ItemStack::getAmount)
                .sum();
    }

    private Map<Material, Integer> countEachOfCategory(RitualContext context, Set<Material> materials) {
        return context.getSacrificedItems().stream()
                .filter(itemStack -> !materials.contains(itemStack.getType()))
                .collect(Collectors.groupingBy(ItemStack::getType, Collectors.summingInt(ItemStack::getAmount)));
    }

    @Override
    public boolean canPerform(RitualContext context) {
        // Check if there are dead players nearby
        boolean performerIsDead = PlayerDataManager.getInstance().getPlayerData(context.getInvoker()).isDead();
        boolean anyNearbyDead = context.getLocation().getNearbyPlayers(ritualRange).stream().anyMatch(
                player -> {
                    if (CitizensAPI.getNPCRegistry().isNPC(player)) return false;
                    return PlayerDataManager.getInstance().getPlayerData(player).isDead();
                }
        );
        if (performerIsDead || !anyNearbyDead) return false;
        return super.canPerform(context);
    }

    private final Map<Material, Integer> reviveHeartsStrength = Map.of(
            Material.SOUL_LANTERN, 10,
            Material.ECHO_SHARD, 5,
            Material.AMETHYST_SHARD, 3
    );

    private int decrementHighestIngredientAndReturnLives(Map<Material, Integer> ritualInventory) {
        Material strongest = null;
        int lives = 0;
        for (Map.Entry<Material, Integer> entry : ritualInventory.entrySet()) {
            // If the inventory has more than 0
            if (entry.getValue() > 0) {
                // Check if it's stronger than current
                if (reviveHeartsStrength.getOrDefault(entry.getKey(), 0) > lives) {
                    // Remember this one
                    lives =  reviveHeartsStrength.get(entry.getKey());
                    strongest = entry.getKey();
                }
            }
        }
        // Something went wrong
        if (strongest == null) {
            AscendancyPlugin.getInstance().getLogger().warning("Something went wrong trying to get the strongest ingredient. Defaulting to 0 (won't revive).");
            return 0;
        }
        ritualInventory.put(strongest, ritualInventory.get(strongest) - 1);
        return lives;
    }

    @Override
    public ActiveRitualContext perform(RitualContext context) {
        int primaryIngredients = countItemsOfCategory(context, primary);
        int supplementaryFirstIngredients = countItemsOfCategory(context, supplementarySecond);
        int supplementarySecondIngredients = countItemsOfCategory(context, supplementaryThird);

        Map<Material, Integer> supplementary = countEachOfCategory(context, supplementaryThird);

        int maxAllowedRevives = Math.min(primaryIngredients, Math.min(supplementaryFirstIngredients, supplementarySecondIngredients));

        List<Player> nearbyDeadPlayers = context.getLocation().getNearbyPlayers(ritualRange).stream().filter(
                player -> {
                    if (CitizensAPI.getNPCRegistry().isNPC(player)) return false;
                    return PlayerDataManager.getInstance().getPlayerData(player).isDead();
                }
        ).limit(maxAllowedRevives).toList();

        List<Player> successfulRevivals = new ArrayList<>();

        nearbyDeadPlayers.forEach(player -> {
            PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(player);
            int reviveLives = decrementHighestIngredientAndReturnLives(supplementary);
            if (reviveLives == 0) {
                return;
            }
            playerData.setLives(reviveLives);
            playerData.setDead(false);
            successfulRevivals.add(player);
            player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation(), 200, 2, 2, 2);
            player.getWorld().playSound(player, Sound.ENTITY_WITHER_SPAWN, 0.5f, 2.0f);
            AscendancyMessagingAPI.getInstance().sendBoxed(player, AscendancyMessagingAPI.MessageType.INFO, "Revival Ritual", null, "You have been revived by " + context.getInvoker().getName());
        });

        String playerNames = successfulRevivals.stream().map(Player::getName).collect(Collectors.joining(", "));
        AscendancyMessagingAPI.getInstance().send(context.getInvoker(), AscendancyMessagingAPI.MessageType.SUCCESS, "You have revived the following players: " + playerNames);

        return super.perform(context);
    }
}
