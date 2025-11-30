package io.github.hyscript7.ascendancy.features.bossprog.listeners;

import io.github.hyscript7.ascendancy.AscendancyConfig;
import io.github.hyscript7.ascendancy.data.players.PlayerData;
import io.github.hyscript7.ascendancy.data.players.PlayerDataManager;
import io.github.hyscript7.ascendancy.features.bossprog.BossType;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.Nullable;

public class BossKillListener implements Listener {
    private final int killCreditRadius;

    public BossKillListener() {
        this.killCreditRadius = AscendancyConfig.getInstance().getBossProgression().killCreditRadius();
    }

    @EventHandler
    public void onBossKill(EntityDeathEvent event) {
        LivingEntity victim = event.getEntity();
        @Nullable BossType bossType = BossType.fromEntity(victim);
        if (bossType == null) return;
        PlayerDataManager playerDataManager = PlayerDataManager.getInstance();
        victim.getLocation().getNearbyPlayers(killCreditRadius).stream()
                .map(player -> new PlayerDataUnion(player, playerDataManager.getPlayerData(player)))
                .forEach(union -> {
                    union.data().addBossKill(bossType);
                    // TODO: Format
                    union.player().sendMessage(
                            Component.text("You have killed the " +
                                            bossType.name().replace("_", " ") + " boss!")
                                    .append(Component.text("\nYou are now at " +
                                            union.data().getKilledBossesCount() + " boss kills!")));
                });
        // TODO: Format
        Bukkit.broadcast(Component.text("The " + bossType.name().replace("_", " ") + " has been killed!"));
    }

    private record PlayerDataUnion(Player player, PlayerData data) {
    }
}
