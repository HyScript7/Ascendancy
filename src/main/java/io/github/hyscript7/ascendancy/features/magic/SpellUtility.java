package io.github.hyscript7.ascendancy.features.magic;

import io.github.hyscript7.ascendancy.AscendancyPlugin;
import io.github.hyscript7.ascendancy.data.players.PlayerData;
import io.github.hyscript7.ascendancy.data.players.PlayerDataManager;
import io.github.hyscript7.ascendancy.registries.RegistryManager;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

public class SpellUtility {

    public static @Nullable Spell getSpellFromString(String s) {
        return RegistryManager.getInstance().getSpellRegistry().getAll().stream()
                .filter(spell -> spell.incantationMatches(s))
                .findFirst().orElse(null);
    }

    public static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z0-9 ]", "");
    }

    public static void runSpell(SpellContext context, Spell spell) {
        if (SpellCooldownManager.getInstance().isPlayerOnCooldown(context.getCaster(), spell)) {
            // TODO: Send cooldown message
            return;
        }
        if (spell.getTier().requiresLearning()) {
            PlayerData playerData = PlayerDataManager.getInstance().getPlayerData(context.getCaster());
            if (!playerData.knowsSpell(spell.getId())) {
                // TODO: Send need to learn message
                return;
            }
        }
        if (spell.canCast(context)) {
            // TODO: Check mana
            // TODO: Send feedback
            SpellCooldownManager.getInstance().setPlayerOnCooldown(context.getCaster(), spell);
            Bukkit.getScheduler().runTask(AscendancyPlugin.getInstance(), () -> {
                spell.cast(context);
            });
        }
    }

}
