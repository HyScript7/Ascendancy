package io.github.Youngcoder45.ascendancy.rituals;

import io.github.hyscript7.ascendancy.features.rituals.*;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class RitualOfTheWolf extends AbstractRitual {
    public RitualOfTheWolf() {
        super("ritual_wolf", "Ritual of the Wolf", RitualGrade.BASIC, buildStages());
    }

    private static List<RitualStage> buildStages() {
        return List.of(
                new RitualStage() {
                    @Override
                    public boolean isComplete(RitualContext context) {
                        long boneCount = context.getSacrificedItems().stream()
                                .filter(item -> item.getType() == Material.BONE)
                                .mapToInt(ItemStack::getAmount)
                                .sum();
                        return boneCount >= 5;
                    }

                    @Override
                    public String getHint(RitualContext context) {
                        return "Sacrifice at least 5 Bones.";
                    }
                },
                new RitualStage() {
                    @Override
                    public boolean isComplete(RitualContext context) {
                        return context.getSacrificedItems().stream()
                                .anyMatch(item -> item.getType() == Material.ROTTEN_FLESH);
                    }

                    @Override
                    public String getHint(RitualContext context) {
                        return "Sacrifice a piece of Rotten Flesh.";
                    }
                }
        );
    }

    @Override
    public ActiveRitualContext perform(RitualContext context) {
        Wolf wolf = (Wolf) context.getLocation().getWorld().spawnEntity(context.getLocation().add(0, 1, 0), EntityType.WOLF);
        wolf.setOwner(context.getInvoker());
        wolf.setTamed(true);
        wolf.setCollarColor(org.bukkit.DyeColor.BLUE);
        return defaultInstantRitualContext(this, context);
    }
}
