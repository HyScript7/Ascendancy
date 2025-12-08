package io.github.hyscript7.ascendancy.builtins.threefolds.rituals.self;

import io.github.hyscript7.ascendancy.AscendancyMessagingAPI;
import io.github.hyscript7.ascendancy.builtins.threefolds.existences.SelfAudience;
import io.github.hyscript7.ascendancy.data.players.PlayerData;
import io.github.hyscript7.ascendancy.data.players.PlayerDataManager;
import io.github.hyscript7.ascendancy.data.players.names.TrueNameManager;
import io.github.hyscript7.ascendancy.features.bossprog.BossType;
import io.github.hyscript7.ascendancy.features.innate.protections.InnateProtectionManager;
import io.github.hyscript7.ascendancy.features.threefold.AbstractThreefoldIncantation;
import io.github.hyscript7.ascendancy.features.threefold.ThreefoldContext;
import io.github.hyscript7.ascendancy.features.voidrealm.VoidRealmLayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.UUID;

public class NameRecall extends AbstractThreefoldIncantation {

    public NameRecall() {
        super("innate name remember", "Innate Name Library", new String[]{
                "Recall all the names I've heard"
        }, SelfAudience.id);
    }

    @Override
    public void execute(ThreefoldContext context) {
        PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(context.player());
        AscendancyMessagingAPI messagingAPI = AscendancyMessagingAPI.getInstance();
        messagingAPI.send(context.player(), AscendancyMessagingAPI.MessageType.INFO, "You recall all the names you have heard:");
        playerData.getKnownTrueNames().forEach(name -> {
            UUID uuid = TrueNameManager.getInstance().findTrueNameOwner(name);
            if (uuid == null) return;
            if (InnateProtectionManager.getInstance().isProtected(uuid)) return;
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            messagingAPI.send(context.player(), AscendancyMessagingAPI.MessageType.INFO, name + " -> " + player.getName());
        });
    }

    @Override
    public boolean canExecute(ThreefoldContext context) {
        return !PlayerDataManager.getInstance().getPlayerData(context.player()).getKnownTrueNames().isEmpty();
    }
}
