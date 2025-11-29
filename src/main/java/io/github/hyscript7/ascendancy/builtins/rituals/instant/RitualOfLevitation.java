package io.github.hyscript7.ascendancy.builtins.rituals.instant;

import io.github.hyscript7.ascendancy.features.rituals.*;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class RitualOfLevitation extends AbstractRitual {
    public RitualOfLevitation() {
        super("instant_levitation", "Ritual of Levitation", RitualGrade.BASIC, buildStages());
    }

    private static List<RitualStage> buildStages() {
        return List.of(
                new RitualStage() {
                    @Override
                    public boolean isComplete(RitualContext context) {
                        return context.getSacrificedItems().contains(ItemStack.of(Material.FEATHER, 1)) || context.getSacrificedEntities().containsKey(EntityType.CHICKEN);
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
        context.getLocation().getNearbyEntitiesByType(Player.class, 5,5,5).stream().forEach(
                player -> player.addPotionEffect(PotionEffectType.LEVITATION.createEffect(5, 99))
        );
        return defaultInstantRitualContext(this, context);
    }
}
