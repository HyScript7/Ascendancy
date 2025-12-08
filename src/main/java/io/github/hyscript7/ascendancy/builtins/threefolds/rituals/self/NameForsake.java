package io.github.hyscript7.ascendancy.builtins.threefolds.rituals.self;

import io.github.hyscript7.ascendancy.AscendancyMessagingAPI;
import io.github.hyscript7.ascendancy.builtins.threefolds.existences.SelfAudience;
import io.github.hyscript7.ascendancy.data.players.PlayerData;
import io.github.hyscript7.ascendancy.data.players.PlayerDataManager;
import io.github.hyscript7.ascendancy.data.players.names.TrueNameManager;
import io.github.hyscript7.ascendancy.features.bossprog.BossType;
import io.github.hyscript7.ascendancy.features.threefold.AbstractThreefoldIncantation;
import io.github.hyscript7.ascendancy.features.threefold.ThreefoldContext;
import io.github.hyscript7.ascendancy.features.voidrealm.VoidRealmLayer;
import org.bukkit.Particle;
import org.bukkit.Sound;

public class NameForsake extends AbstractThreefoldIncantation {

    public NameForsake() {
        super("innate name forsake", "Forsake Innate Name", new String[]{
                "Confess that I am a sinner",
                "Have proven that I have changed",
                "and I forsake my true name"
        }, SelfAudience.id);
    }

    @Override
    public void execute(ThreefoldContext context) {
        PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(context.player());
        playerData.removeBossKill(BossType.REFLECTION_OF_SELF);
        String newName = TrueNameManager.getInstance().generateAndRegisterTrueName(context.player().getUniqueId());
        playerData.setTrueName(newName);
        AscendancyMessagingAPI.getInstance().send(context.player(), AscendancyMessagingAPI.MessageType.INFO, "You are now called <bold>" + playerData.getTrueName() + "</bold>");
        context.player().playSound(context.player().getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.0f);
        context.player().spawnParticle(Particle.END_ROD, context.player().getLocation().add(0, 1, 0), 40, 0.4, 0.5, 0.4);
        context.player().spawnParticle(Particle.SOUL, context.player().getLocation().add(0, 1, 0), 40, 0.4, 0.5, 0.4);
    }

    @Override
    public boolean canExecute(ThreefoldContext context) {
        boolean isAtReflectionOfSelf = VoidRealmLayer.fromWorld(context.player().getLocation().getWorld()) == VoidRealmLayer.REFLECTION;
        PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(context.player());
        boolean hasReflectionOfSelfKill = playerData.hasKilledBoss(BossType.REFLECTION_OF_SELF);
        return hasReflectionOfSelfKill && isAtReflectionOfSelf;
    }
}
