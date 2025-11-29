package io.github.hyscript7.ascendancy.features.rituals;

import io.github.hyscript7.ascendancy.registries.Identifiable;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface Ritual extends Identifiable {
    /**
     * Returns an ordered list of all stages in this ritual.
     * @return An ordered, unmodifiable collection
     */
    List<RitualStage> getStages();

    /**
     * Returns the current stage which we're waiting on.
     * @param context The ritual context
     * @return The first incomplete stage.
     */
    RitualStage getCurrentStage(RitualContext context);

    /**
     * What happens after a ritual is activated.
     * <p>
     * You can perform additional checks in canPerform to cancel the ritual
     * activation if
     * @param context The ritual context
     * @return True if the ritual succeeded, otherwise false.
     */
    ActiveRitualContext perform(RitualContext context);

    /**
     * Allows you to run various checks before perform is called.
     * <p>
     * <b>THIS METHOD SHOULDN'T HAVE ANY SIDE EFFECTS</b>
     * @param context The ritual context
     * @return true if the ritual can be performed, otherwise false
     */
    boolean canPerform(RitualContext context);

    /**
     * Checks whether the used catalyst can be used with this ritual.
     * @param itemStack The used catalyst
     * @return True if it can, false otherwise
     */
    boolean catalystAppropriate(ItemStack itemStack);
}
