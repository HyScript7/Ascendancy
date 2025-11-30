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

    @Builder.Default
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
        // Already identified, nothing to do
        if (identifiedRitual.isPresent()) {
            return;
        }

        // Initialize possible rituals on first call
        if (possibleRituals == null) {
            possibleRituals = List.copyOf(RegistryManager.getInstance().getRitualRegistry().getAll());
        }

        // No rituals available
        if (possibleRituals.isEmpty()) {
            return;
        }

        // Filter and sort rituals by completion
        possibleRituals = possibleRituals.stream()
                .filter(ritual -> ritual.catalystAppropriate(catalyst))
                .map(ritual -> new Intermediary(ritual, ritual.getStages().stream()
                        .filter(stage -> stage.isComplete(this)).count()))
                .sorted(Comparator.comparingLong(Intermediary::completedStages).reversed()) // DESCENDING order
                .map(Intermediary::ritual)
                .toList();

        // Check if we can uniquely identify a ritual
        if (possibleRituals.isEmpty()) {
            return; // No matching rituals
        }

        if (possibleRituals.size() == 1) {
            identifiedRitual = Optional.of(possibleRituals.getFirst());
        } else {
            // Get the top ritual(s) by completion count
            Ritual topRitual = possibleRituals.getFirst();
            long topCompletedStages = topRitual.getStages().stream()
                    .filter(stage -> stage.isComplete(this)).count();

            // Check if there's a clear winner (no ties at the top)
            long secondBestStages = possibleRituals.size() > 1
                    ? possibleRituals.get(1).getStages().stream()
                    .filter(stage -> stage.isComplete(this)).count()
                    : -1;

            if (topCompletedStages > secondBestStages) {
                identifiedRitual = Optional.of(topRitual);
            }
            // Otherwise, keep possibleRituals for next attempt
        }
    }

}
