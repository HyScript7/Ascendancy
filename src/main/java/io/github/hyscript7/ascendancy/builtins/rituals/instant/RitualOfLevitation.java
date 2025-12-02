package io.github.hyscript7.ascendancy.builtins.rituals.instant;

import io.github.hyscript7.ascendancy.AscendancyPlugin;
import io.github.hyscript7.ascendancy.features.rituals.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;

public class RitualOfLevitation extends AbstractRitual {
    public RitualOfLevitation() {
        super("ritual levitation", "Levitation", RitualGrade.BASIC, buildStages());
    }

    private static List<RitualStage> buildStages() {
        return List.of(
                new RitualStage() {
                    @Override
                    public boolean isComplete(RitualContext context) {
                        return context.getSacrificedItems().stream().anyMatch(itemStack -> itemStack.getType().equals(Material.FEATHER)) || context.getSacrificedEntities().containsKey(EntityType.CHICKEN);
                    }

                    @Override
                    public String getHint(RitualContext context) {
                        return "Add 1x feather or sacrifice a chicken.";
                    }
                }
        );
    }

    @Override
    public ActiveRitualContext perform(RitualContext context) {
        int sacrificedItems = context.getSacrificedItems().stream().filter(itemStack -> itemStack.getType().equals(Material.FEATHER)).map(ItemStack::getAmount).findFirst().orElse(0);
        int sacrificedMobs = context.getSacrificedEntities().entrySet().stream().filter(kv -> kv.getKey().equals(EntityType.CHICKEN)).map(Map.Entry::getValue).findFirst().orElse(0);
        int strength = (int) Math.min(125, ((Math.log((sacrificedMobs * 2) + sacrificedItems) / Math.log(2))+1) * 20);
        int duration = (int) (Math.min(10, Math.pow((sacrificedItems + sacrificedMobs * 2),2)) * 20);
        AscendancyPlugin.getInstance().getLogger().info("Levitation ritual casted with duration " + duration + " and strength " + strength);
        context.getLocation().getNearbyEntitiesByType(Player.class, 5,5,5).forEach(
                player -> {
                    player.addPotionEffect(PotionEffectType.LEVITATION.createEffect(duration, strength));
                    if (RitualGrade.fromCatalyst(context.getCatalyst().getType()).greaterThan(RitualGrade.INTERMEDIATE)) {
                        Bukkit.getScheduler().runTaskLater(AscendancyPlugin.getInstance(), new Parachute(player), duration);
                    }
                }
        );
        return defaultInstantRitualContext(this, context);
    }

    private static record Parachute(Player player) implements Runnable {
        @Override
        public void run() {
            if (player.isOnline()) {
                int timeInTicks = 15 * 20; // 15 seconds
                player.addPotionEffect(PotionEffectType.SLOW_FALLING.createEffect(timeInTicks, 0));
            }
        }
    }
}
