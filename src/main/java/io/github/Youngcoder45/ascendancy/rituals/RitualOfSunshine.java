package io.github.Youngcoder45.ascendancy.rituals;

import io.github.hyscript7.ascendancy.features.rituals.*;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.List;

public class RitualOfSunshine extends AbstractRitual {
    public RitualOfSunshine() {
        super("ritual_sunshine", "Ritual of Sunshine", RitualGrade.BASIC, buildStages());
    }

    private static List<RitualStage> buildStages() {
        return List.of(
                new RitualStage() {
                    @Override
                    public boolean isComplete(RitualContext context) {
                        return context.getSacrificedItems().stream().anyMatch(item -> item.getType() == Material.SUNFLOWER) ||
                               context.getSacrificedItems().stream().anyMatch(item -> item.getType() == Material.GOLD_INGOT);
                    }

                    @Override
                    public String getHint(RitualContext context) {
                        return "Sacrifice a Sunflower or a Gold Ingot.";
                    }
                }
        );
    }

    @Override
    public ActiveRitualContext perform(RitualContext context) {
        World world = context.getLocation().getWorld();
        if (world != null) {
            world.setTime(1000); // Set to morning
            world.setStorm(false);
            world.setThundering(false);
        }
        return defaultInstantRitualContext(this, context);
    }
}
