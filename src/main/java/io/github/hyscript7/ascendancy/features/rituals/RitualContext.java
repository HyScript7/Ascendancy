package io.github.hyscript7.ascendancy.features.rituals;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Builder
@Getter
public class RitualContext {
    private final Player invoker;
    private final Location location;
    private final ItemStack catalyst; RitualGrade grade;
    private final List<ItemStack> sacrificedItems;
    private final Map<EntityType, Integer> sacrificedEntities;
    private final List<RitualStage> completedStages;
}
