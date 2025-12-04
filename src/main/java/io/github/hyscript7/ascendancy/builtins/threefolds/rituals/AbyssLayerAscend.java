package io.github.hyscript7.ascendancy.builtins.threefolds.rituals;

import io.github.hyscript7.ascendancy.AscendancyPlugin;
import io.github.hyscript7.ascendancy.builtins.threefolds.existences.ThePrimordialAbyss;
import io.github.hyscript7.ascendancy.features.threefold.AbstractThreefoldIncantation;
import io.github.hyscript7.ascendancy.features.threefold.ThreefoldContext;
import io.github.hyscript7.ascendancy.features.voidrealm.LayerChanger;
import io.github.hyscript7.ascendancy.features.voidrealm.VoidRealmLayer;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AbyssLayerAscend extends AbstractThreefoldIncantation {
    private final LayerChanger layerChanger = new LayerChanger();

    public AbyssLayerAscend() {
        super("abyss ascent", "Abysmal Ascent", new String[]{
                "I pray for your attention",
                "I pray for you to let me exit this empty realm"
        }, ThePrimordialAbyss.id);
    }

    @Override
    public void execute(ThreefoldContext context) {
        Player player = context.player();
        VoidRealmLayer oldLayer = getPlayersLayer(player);
        if (!(oldLayer == VoidRealmLayer.ABYSS && player.isDead())) {
            player.addPotionEffect(PotionEffectType.LEVITATION.createEffect(10 * 20, 99));
        }
    }

    @Override
    public boolean canExecute(ThreefoldContext context) {
        if (context.player().isDead()) return false;
        VoidRealmLayer layer = getPlayersLayer(context.player());
        // No escape from Reflection of Self
        return layer != VoidRealmLayer.REFLECTION && layer != null;
    }

    private @Nullable VoidRealmLayer getPlayersLayer(Player player) {
        return VoidRealmLayer.fromWorld(player.getLocation().getWorld());
    }
}
