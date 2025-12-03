package eu.projnull.spelis.ascendancy.rituals;

import io.github.hyscript7.ascendancy.AscendancyPlugin;
import io.github.hyscript7.ascendancy.features.rituals.*;
import io.github.hyscript7.ascendancy.features.rituals.requirements.ItemExclusivityFactory;
import io.github.hyscript7.ascendancy.features.rituals.requirements.ItemSacrifice;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Set;

public class RitualOfTheAnchor extends AbstractRitual {
    public RitualOfTheAnchor() {
        super("ritual_anchor","Ritual of the Anchor", RitualGrade.ADVANCED,buildStages());
    }

    private static final ItemExclusivityFactory exclusivity = new ItemExclusivityFactory(Set.of(Material.ENDER_PEARL, Material.COMPASS, Material.LODESTONE));

    private static List<RitualStage> buildStages() {
        return List.of(
                exclusivity.on(
                        ItemSacrifice.builder().itemType(Material.LODESTONE).minAmount(1).build()
                ),
                exclusivity.on(
                        ItemSacrifice.builder().itemType(Material.COMPASS).minAmount(1).build()
                ),
                exclusivity.on(
                        ItemSacrifice.builder().itemType(Material.ENDER_PEARL).minAmount(5).build()
                )
        );
    }

    @Override
    public ActiveRitualContext perform(RitualContext context) {
        Location location = context.getLocation().clone();
        location.setY(location.getY()-1);
        Block lodestoneBlock = location.getWorld().getBlockAt(location);
        lodestoneBlock.setType(Material.LODESTONE);

        ItemStack compassItem = new ItemStack(Material.COMPASS);
        compassItem.setAmount(1);
        if (compassItem.getItemMeta() instanceof CompassMeta compassMeta) {
            compassMeta.setLodestone(location);
            compassMeta.setLodestoneTracked(true);
            compassMeta.getPersistentDataContainer().set(new NamespacedKey(AscendancyPlugin.getInstance(),"teleportation_compass"), PersistentDataType.BOOLEAN, true);
            compassItem.setItemMeta(compassMeta);
        }
        context.getInvoker().give(compassItem);

        return defaultInstantRitualContext(this,context);
    }
}