package io.github.hyscript7.ascendancy.builtins.rituals.instant;

import io.github.hyscript7.ascendancy.AscendancyMessagingAPI;
import io.github.hyscript7.ascendancy.features.rituals.*;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

public class RitualOfHomeComing extends AbstractRitual {

    // players in this range of the ritual will get teleported
    private static final double TARGET_RADIUS = 2.5d;

    public RitualOfHomeComing() {
        super("ritual teleport home", "Home Coming", RitualGrade.INTERMEDIATE, buildStages());
    }

    private static List<RitualStage> buildStages() {
        return List.of(
                new RitualStage() {
                    @Override
                    public boolean isComplete(RitualContext context) {
                        return context.getSacrificedItems().stream().anyMatch(item -> item.getType().equals(Material.COMPASS));
                    }

                    @Override
                    public String getHint(RitualContext context) {
                        return "Sacrifice a compass";
                    }
                }
        );
    }

    @Override
    public boolean catalystAppropriate(ItemStack itemStack) {
        Material material = itemStack.getType();
        RitualGrade catalyst = RitualGrade.fromCatalyst(material);
        if (catalyst == null) return false; // the fuck?
        return material.equals(Material.ENDER_EYE) || material.equals(Material.ENDER_PEARL) || catalyst.greaterThan(RitualGrade.ADVANCED);
    }

    @Override
    public ActiveRitualContext perform(RitualContext context) {
        final Location location = Optional.ofNullable(context.getInvoker().getRespawnLocation()).orElse(Bukkit.getWorld("world").getSpawnLocation());
        final int particleCount = (int) Math.round(TARGET_RADIUS*100);
        context.getLocation().getNearbyPlayers(TARGET_RADIUS).forEach(player -> {
            player.teleport(location);
            player.spawnParticle(Particle.PORTAL, location, particleCount, TARGET_RADIUS, TARGET_RADIUS, TARGET_RADIUS);
            player.playSound(player, Sound.BLOCK_PORTAL_TRAVEL, 0.5f, 1.0f);
            AscendancyMessagingAPI.getInstance().send(player, AscendancyMessagingAPI.MessageType.SUCCESS, "You teleported to " + (context.getInvoker().equals(player) ? "your" : context.getInvoker().getName() + "'s") + " home.");
        });
        context.getLocation().getWorld().spawnParticle(Particle.REVERSE_PORTAL, context.getLocation(), particleCount, TARGET_RADIUS, TARGET_RADIUS, TARGET_RADIUS);
        context.getLocation().getWorld().playSound(context.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, 0.5f, 1.0f);
        return defaultInstantRitualContext(this, context);
    }
}
