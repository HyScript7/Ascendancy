package io.github.youngcoder45.ascendancy.spells;

import io.github.hyscript7.ascendancy.AscendancyPlugin;
import io.github.hyscript7.ascendancy.features.magic.AbstractRegexIncantationSpell;
import io.github.hyscript7.ascendancy.features.magic.SpellContext;
import io.github.hyscript7.ascendancy.features.magic.SpellTier;
import org.bukkit.entity.SmallFireball;

public class Ignis extends AbstractRegexIncantationSpell {
    public Ignis() {
        super("ignis", "Ignis", ".*\\b([Ii]gnis|[Ii]ncendio|[Ff]lamara)\\b.*", SpellTier.COMMON, 20, 5 * 1000);
    }

    @Override
    public boolean cast(SpellContext context) {
        context.getCaster().launchProjectile(SmallFireball.class);
        return true;
    }

    @Override
    public boolean canCast(SpellContext context) {
        return true;
    }

    private static final String[] incantations = new String[] {"Ignis", "Incendio", "Flamara"};

    @Override
    public String getIncantation() {
        return incantations[AscendancyPlugin.getInstance().getRandom().nextInt(incantations.length)];
    }
}
