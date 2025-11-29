package io.github.hyscript7.ascendancy.features.rituals;

import io.github.hyscript7.ascendancy.registries.RegistryManager;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
@Builder
public class RitualContext {
    @Getter
    private final Player invoker;
    @Getter
    private final Location location;
    @Getter
    private final ItemStack catalyst;
    @Getter
    RitualGrade grade;
    @Getter
    private final List<ItemStack> sacrificedItems;
    @Getter
    private final Map<EntityType, Integer> sacrificedEntities;
    @Getter
    private final List<RitualStage> completedStages;
    @Builder.Default
    private Optional<Ritual> identifiedRitual = Optional.empty();

    private List<Ritual> possibleRituals = null;

    public @Nullable Ritual getIdentifiedRitual() {
        return identifiedRitual.orElse(null);
    }

    public boolean isRitualIdentified() {
        attemptToIdentifyRitual();
        return identifiedRitual.isPresent();
    }

    public boolean isRitualReady() {
        if (!isRitualIdentified()) return false;
        return getIdentifiedRitual().canPerform(this);
    }

    private record Intermediary(Ritual ritual, long completedStages) {}

    private void attemptToIdentifyRitual() {
        if (possibleRituals == null) {
            possibleRituals = List.copyOf(RegistryManager.getInstance().getRitualRegistry().getAll());
        }
        if (possibleRituals.isEmpty() || identifiedRitual.isPresent()) {
            return;
        }
        possibleRituals = possibleRituals.stream()
                .filter(ritual -> ritual.catalystAppropriate(catalyst))
                .map(ritual -> new Intermediary(ritual, ritual.getStages().stream()
                        .filter(stage -> stage.isComplete(this)).count()))
                .sorted(Comparator.comparingInt(im -> (int) (im.completedStages())))
                .map(Intermediary::ritual).toList();
        if (possibleRituals.size() == 1) {
            identifiedRitual = Optional.of(possibleRituals.getFirst());
        }
    }

}
