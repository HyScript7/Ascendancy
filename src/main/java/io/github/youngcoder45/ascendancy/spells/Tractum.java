package io.github.youngcoder45.ascendancy.spells;

import io.github.hyscript7.ascendancy.AscendancyPlugin;
import io.github.hyscript7.ascendancy.features.magic.AbstractRegexIncantationSpell;
import io.github.hyscript7.ascendancy.features.magic.SpellContext;
import io.github.hyscript7.ascendancy.features.magic.SpellTier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class Tractum extends AbstractRegexIncantationSpell {
    public Tractum() {
        super("tractum", "Tractum", ".*\\b([Tt]ractum|[Aa]ccio|[Vv]enite)\\b.*", SpellTier.UNCOMMON, 30, 10 * 1000);
    }

    @Override
    public boolean cast(SpellContext context) {
        RayTraceResult result = context.getCaster().getWorld().rayTraceEntities(
                context.getCaster().getEyeLocation(),
                context.getCaster().getEyeLocation().getDirection(),
                25,
                1.0,
                entity -> entity != context.getCaster()
        );

        if (result != null && result.getHitEntity() != null) {
            Entity target = result.getHitEntity();
            Vector direction = context.getCaster().getLocation().toVector().subtract(target.getLocation().toVector()).normalize();
            target.setVelocity(direction.multiply(1.5)); // Pull towards caster
            return true;
        }
        return false;
    }

    @Override
    public boolean canCast(SpellContext context) {
        return true;
    }

    private static final String[] incantations = new String[] {"Tractum", "Accio", "Venite"};

    @Override
    public String getIncantation() {
        return incantations[AscendancyPlugin.getInstance().getRandom().nextInt(incantations.length)];
    }
}
