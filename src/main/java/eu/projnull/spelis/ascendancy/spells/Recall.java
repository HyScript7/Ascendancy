package eu.projnull.spelis.ascendancy.spells;

import io.github.hyscript7.ascendancy.AscendancyPlugin;
import io.github.hyscript7.ascendancy.features.magic.AbstractRegexIncantationSpell;
import io.github.hyscript7.ascendancy.features.magic.SpellContext;
import io.github.hyscript7.ascendancy.features.magic.SpellTier;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;

public class Recall extends AbstractRegexIncantationSpell {
    public Recall() {
        super("recall", "Recall", ".*\\b[Rr]ecall\\b.*", SpellTier.RARE, 60, 20*1000);
    }

    @Override
    public boolean cast(SpellContext context) {
        ItemStack mainHandItem = context.getCaster().getInventory().getItemInMainHand();
        if (mainHandItem.getType() != Material.COMPASS) return false;
        if (mainHandItem.getItemMeta() instanceof CompassMeta compassMeta) {
            NamespacedKey telecompass_key = new NamespacedKey(AscendancyPlugin.getInstance(),"teleportation_compass");
            if (compassMeta.hasLodestone() && compassMeta.getPersistentDataContainer().has(telecompass_key)) {
                Location lodestoneLocation = compassMeta.getLodestone();
                assert lodestoneLocation != null;
                if (lodestoneLocation.getBlock().getType() != Material.LODESTONE) return false; // the compass' link is not valid.
                lodestoneLocation.setY(lodestoneLocation.getY()+1);
                context.getCaster().teleport(lodestoneLocation);
            }
        }
        else {
            return false;
        }
        return true;
    }

    @Override
    public boolean canCast(SpellContext context) {
        return true;
    }

    private static final String[] incantations = new String[] {"Recall"};

    @Override
    public String getIncantation() {
        return incantations[AscendancyPlugin.getInstance().getRandom().nextInt(incantations.length)];
    }
}
