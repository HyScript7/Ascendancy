package io.github.hyscript7.ascendancy.builtins.threefolds.rituals.self;

import io.github.hyscript7.ascendancy.builtins.threefolds.existences.SelfAudience;
import io.github.hyscript7.ascendancy.data.players.PlayerData;
import io.github.hyscript7.ascendancy.data.players.PlayerDataManager;
import io.github.hyscript7.ascendancy.features.threefold.AbstractThreefoldIncantation;
import io.github.hyscript7.ascendancy.features.threefold.ThreefoldContext;
import io.github.hyscript7.ascendancy.features.voidrealm.VoidRealmLayer;
import org.bukkit.Bukkit;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Objects;
import java.util.Optional;

public class EscapeReflection extends AbstractThreefoldIncantation {
    public EscapeReflection() {
        super("reflection escape", "Escape Reflection", new String[]{
                "Return myself to the realm of soul",
        }, SelfAudience.id);
    }

    @Override
    public void execute(ThreefoldContext context) {
        context.player().teleport(
                Optional.ofNullable(
                        context.player().getRespawnLocation()
                ).orElse(
                        Objects.requireNonNull(Bukkit.getWorld("world")).getSpawnLocation()
                )
        );
        context.player().addPotionEffect(
                PotionEffectType.RESISTANCE.createEffect(30 * 20, 9)
        );
        context.player().addPotionEffect(
                PotionEffectType.DARKNESS.createEffect(30 * 20, 9)
        );
    }

    @Override
    public boolean canExecute(ThreefoldContext context) {
        boolean isAtReflectionOfSelf = VoidRealmLayer.fromWorld(context.player().getLocation().getWorld()) == VoidRealmLayer.REFLECTION;
        PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(context.player());
        boolean hasResurrection = playerData.getResurrection() > 0;
        return hasResurrection && isAtReflectionOfSelf;
    }
}
