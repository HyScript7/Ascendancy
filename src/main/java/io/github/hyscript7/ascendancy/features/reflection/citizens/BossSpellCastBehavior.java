package io.github.hyscript7.ascendancy.features.reflection.citizens;

import io.github.hyscript7.ascendancy.AscendancyPlugin;
import io.github.hyscript7.ascendancy.features.magic.AbstractSpell;
import io.github.hyscript7.ascendancy.features.magic.Spell;
import io.github.hyscript7.ascendancy.features.magic.SpellContext;
import io.github.hyscript7.ascendancy.features.magic.SpellTier;
import io.github.hyscript7.ascendancy.registries.RegistryManager;
import io.papermc.paper.entity.LookAnchor;
import net.citizensnpcs.api.ai.tree.BehaviorGoalAdapter;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class BossSpellCastBehavior extends BehaviorGoalAdapter {
    private final NPC npc;
    private int ticksSinceLastCast = 0;
    private int ticksUntilNextCast;
    private final double chaseRadius;

    // TODO: Get a list of spells usable by NPCs and check where it intersects with the player's learned/used spells.
    //       NPCs also have some special spells like Mirum to make the boss harder to cheeze
    private final List<Spell> allowedSpells = List.of(
            RegistryManager.getInstance().getSpellRegistry().get("fireball").get(),
            RegistryManager.getInstance().getSpellRegistry().get("ventus").get(),
            RegistryManager.getInstance().getSpellRegistry().get("fulmen").get(),
            RegistryManager.getInstance().getSpellRegistry().get("sanatio").get(),
            new AbstractSpell("mirum", "Mirum", SpellTier.RARE, 30, 15 * 1000) {

                @Override
                public boolean cast(SpellContext context) {
                    context.getLocation().getNearbyPlayers(chaseRadius, chaseRadius).stream().findFirst().ifPresent(player -> {
                        context.getCaster().teleport(player);
                    });
                    return true;
                }

                @Override
                public boolean canCast(SpellContext context) {
                    return !context.getLocation().getNearbyPlayers(chaseRadius, chaseRadius).isEmpty();
                }

                @Override
                public boolean incantationMatches(SpellContext context) {
                    return true;
                }

                @Override
                public boolean incantationMatches(String string) {
                    return true;
                }

                @Override
                public String getIncantation() {
                    return "Mirum";
                }
            }
    );

    private final Random random = AscendancyPlugin.getInstance().getRandom();

    public BossSpellCastBehavior(NPC npc, double chaseRadius) {
        this.npc = npc;
        this.chaseRadius = chaseRadius;
        this.ticksUntilNextCast = (3 + random.nextInt(6)) * 20; // 3-7 seconds
    }

    @Override
    public void reset() {
        ticksSinceLastCast = 0;
    }

    @Override
    public BehaviorStatus run() {
        // Check if NPC is still spawned
        if (!npc.isSpawned()) {
            return BehaviorStatus.FAILURE;
        }

        // Check if NPC entity is a player
        if (!(npc.getEntity() instanceof Player)) {
            return BehaviorStatus.RUNNING;
        }

        ticksSinceLastCast++;

        // Time to cast a spell?
        if (ticksSinceLastCast >= ticksUntilNextCast) {
            castRandomSpell();

            // Reset for next cast
            ticksSinceLastCast = 0;
            ticksUntilNextCast = (3 + random.nextInt(6)) * 20; // 3-7 seconds
        }

        return BehaviorStatus.RUNNING;
    }

    @Override
    public boolean shouldExecute() {
        // Always execute as long as NPC is spawned
        return npc.isSpawned();
    }

    private void castRandomSpell() {
        Player npcPlayer = (Player) npc.getEntity();

        if (allowedSpells.isEmpty()) {
            return;
        }

        // Look at nearest player
        Optional<Player> nearestPlayer = npc.getEntity().getLocation().getNearbyPlayers(chaseRadius, chaseRadius).stream().filter(entity -> !entity.equals(npcPlayer)).findFirst();
        if (nearestPlayer.isEmpty()) return;
        npcPlayer.lookAt(nearestPlayer.get().getLocation(), LookAnchor.EYES);

        // Pick a random spell
        Spell spell = allowedSpells.get(random.nextInt(allowedSpells.size()));

        // Cast the spell with the NPC as the caster
        try {
            SpellContext context = SpellContext.builder()
                    .caster(npcPlayer)
                    .rawIncantation(spell.getIncantation())
                    .location(npcPlayer.getLocation())
                    .build();

            if (spell.canCast(context)) {
                AscendancyPlugin.getInstance().getLogger().info("NPC now casting " + spell.getDisplayName());
                spell.cast(context);
            } else {
                AscendancyPlugin.getInstance().getLogger().info("NPC won't cast " + spell.getDisplayName() + ": Conditions not met.");
            }

            npcPlayer.getLocation().getNearbyPlayers(chaseRadius, chaseRadius)
                .forEach(p -> p.sendMessage("<" + npc.getFullName() + "> " + spell.getIncantation()));

            npcPlayer.getLocation().getWorld().playSound(npcPlayer.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, 0.5f, 1.0f);

        } catch (Exception e) {
            AscendancyPlugin.getInstance().getLogger().warning("Failed to cast spell for NPC: " + e.getMessage());
        }
    }
}
