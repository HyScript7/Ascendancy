package io.github.youngcoder45.ascendancy.spells;

import io.github.hyscript7.ascendancy.AscendancyPlugin;
import io.github.hyscript7.ascendancy.features.magic.AbstractRegexIncantationSpell;
import io.github.hyscript7.ascendancy.features.magic.SpellContext;
import io.github.hyscript7.ascendancy.features.magic.SpellTier;
import org.bukkit.FluidCollisionMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;

public class Glacius extends AbstractRegexIncantationSpell {
    public Glacius() {
        super("glacius", "Glacius", ".*\\b([Gg]lacius|[Ss]tirps\\s[Gg]lacialis|[Ff]reeze)\\b.*", SpellTier.UNCOMMON, 40, 15 * 1000);
    }

    @Override
    public boolean cast(SpellContext context) {
        RayTraceResult result = context.getCaster().getWorld().rayTraceEntities(
                context.getCaster().getEyeLocation(),
                context.getCaster().getEyeLocation().getDirection(),
                20,
                0.5,
                entity -> entity != context.getCaster() && entity instanceof LivingEntity
        );

        if (result != null && result.getHitEntity() != null) {
            LivingEntity target = (LivingEntity) result.getHitEntity();
            target.setFreezeTicks(140); // 7 seconds of freezing
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 140, 2));
            return true;
        }
        return false;
    }

    @Override
    public boolean canCast(SpellContext context) {
        return true;
    }

    private static final String[] incantations = new String[] {"Glacius", "Stirps Glacialis", "Freeze"};

    @Override
    public String getIncantation() {
        return incantations[AscendancyPlugin.getInstance().getRandom().nextInt(incantations.length)];
    }
}
