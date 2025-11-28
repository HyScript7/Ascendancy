package io.github.hyscript7.ascendancy.features.voidrealm.listeners;

import io.github.hyscript7.ascendancy.AscendancyConfig;
import io.github.hyscript7.ascendancy.features.voidrealm.VoidRealmLayer;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Handles changing layers & entering the void realm
 */
public class VoidRealmLayerChanger implements Listener {

    private final double escapeThresholdPercentage;

    public VoidRealmLayerChanger() {
        escapeThresholdPercentage = 1.0d + AscendancyConfig.getInstance().getVoidRealm().escapeHeightOvershootPercentage() / 100.0d;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVoidDamage(EntityDamageEvent event) {
        if (event.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
            VoidRealmLayer layer = VoidRealmLayer.fromWorld(event.getEntity().getWorld());
            if (layer != null) {
                switch (layer) {
                    case ABYSS -> changeLayer(event.getEntity(), VoidRealmLayer.OBLIVION);
                    // All roads lead to Reflection of Self
                    case OBLIVION, REFLECTION -> changeLayer(event.getEntity(), VoidRealmLayer.REFLECTION);
                    default -> {}
                }
            } else {
                switch (event.getEntity()) {
                    case Player playerEntity ->
                        // Teleport players who jump into the void out of combat into the Void Realm
                        // TODO: If in combat, don't teleport.
                            changeLayer(playerEntity, VoidRealmLayer.ABYSS);
                    case LivingEntity entity -> {
                        // Save mobs with more than 50% HP
                        double maxHealth = Optional.ofNullable(entity.getAttribute(Attribute.MAX_HEALTH)).map(AttributeInstance::getValue).orElse(20.0d);
                        if (entity.getHealth() > (maxHealth / 2)) {
                            changeLayer(entity, VoidRealmLayer.ABYSS);
                        }
                    }
                    case Item itemEntity ->
                        // Always save items
                            changeLayer(itemEntity, VoidRealmLayer.ABYSS);
                    default -> {}
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        VoidRealmLayer layer = VoidRealmLayer.fromWorld(event.getPlayer().getWorld());
        if (layer != null) {
            if (event.getPlayer().getLocation().getY() < layer.getWorld().getLogicalHeight() * escapeThresholdPercentage) {
                return;
            }
            switch (layer) {
                case ABYSS -> changeToOverworld(event.getPlayer());
                case OBLIVION -> changeLayer(event.getPlayer(), VoidRealmLayer.ABYSS);
                case REFLECTION -> changeLayer(event.getPlayer(), VoidRealmLayer.REFLECTION);
                default -> {}
            }
        }
    }

    private void changeLayer(Entity entity, @Nullable VoidRealmLayer newLayer) {
        if (newLayer == null) {
            return;
        }
        switch (newLayer) {
            case ABYSS -> {
                Location location = entity.getLocation();
                location.setY(VoidRealmLayer.ABYSS.getWorld().getMaxHeight());
                location.setWorld(VoidRealmLayer.ABYSS.getWorld());
                entity.teleport(location);
            }
            case OBLIVION -> {
                Location location = entity.getLocation();
                location.setY(VoidRealmLayer.OBLIVION.getWorld().getMaxHeight());
                location.setWorld(VoidRealmLayer.OBLIVION.getWorld());
                entity.teleport(location);
            }
            case REFLECTION -> {
                World world = VoidRealmLayer.REFLECTION.getWorld();
                Location location = new Location(world, 0, world.getMaxHeight(), 0, 0, 0);
                entity.teleport(location);
            }
        }
    }

    private void changeToOverworld(Entity entity) {
        Location location = entity.getLocation();
        location.setWorld(Bukkit.getWorld("minecraft:overworld")); // Not sorry for hardcoding this
        entity.teleport(location);
    }

}
