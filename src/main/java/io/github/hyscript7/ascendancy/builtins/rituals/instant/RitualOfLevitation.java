package io.github.hyscript7.ascendancy.builtins.rituals.instant;

import io.github.hyscript7.ascendancy.features.rituals.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class RitualOfLevitation extends AbstractRitual {
    public RitualOfLevitation() {
        super("ritual levitation", "Ritual of Levitation", RitualGrade.BASIC, buildStages());
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
        long sacrificedItems = context.getSacrificedItems().stream().filter(itemStack -> itemStack.getType().equals(Material.FEATHER)).count();
        int sacrificedMobs = context.getSacrificedEntities().entrySet().stream().filter(kv -> kv.getKey().equals(EntityType.CHICKEN)).map(Map.Entry::getValue).findFirst().orElse(0);
        int strength = Math.min(126, (int) (Math.log((sacrificedMobs * 2L) + sacrificedItems) / Math.log(2)) * 10);
        int duration = Math.min(25, (int) Math.pow((sacrificedItems + sacrificedMobs * 2L),2));
        context.getLocation().getNearbyEntitiesByType(Player.class, 5,5,5).forEach(
                player -> player.addPotionEffect(PotionEffectType.LEVITATION.createEffect(duration*20, strength))
        );
        return defaultInstantRitualContext(this, context);
    }
}
