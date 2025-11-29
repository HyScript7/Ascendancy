package io.github.hyscript7.ascendancy.features.magic.listeners;

import io.github.hyscript7.ascendancy.AscendancyConfig;
import io.github.hyscript7.ascendancy.AscendancyPlugin;
import io.github.hyscript7.ascendancy.features.magic.Spell;
import io.github.hyscript7.ascendancy.features.magic.SpellTier;
import io.github.hyscript7.ascendancy.registries.RegistryManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.*;
import java.util.logging.Logger;

public class LootGenerator implements Listener {
    private static final Map<SpellTier, Double> spellDropChances = getSpellDropChances();

    private static Map<SpellTier, Double> getSpellDropChances() {
        Map<SpellTier, Double> map = new HashMap<>();
        map.put(SpellTier.COMMON, .4d);
        map.put(SpellTier.UNCOMMON, .2d);
        map.put(SpellTier.RARE, .15d);
        map.put(SpellTier.EPIC, .05d);
        return map;
    }

    private final Map<SpellTier, Set<Spell>> spellCache;
    private final List<Float> tornSpellBookCustomModelData;

    public LootGenerator() {
        tornSpellBookCustomModelData = AscendancyConfig.getInstance().getSpellBooks().customModelData();
        spellCache = new HashMap<>();
        RegistryManager.getInstance().getSpellRegistry().getAll().forEach(spell -> {
            spellCache.computeIfAbsent(spell.getTier(), spellTier -> new HashSet<>()).add(spell);
        });
    }

    @EventHandler
    public void onLootChestOpen(LootGenerateEvent event) {
        Logger logger = AscendancyPlugin.getInstance().getLogger();
        SpellTier spellRarity;
        Location lootLocation = event.getLootContext().getLocation();
        World world = lootLocation.getWorld();
        int y = lootLocation.getBlockY();
        double distanceFromSpawn = new Location(lootLocation.getWorld(), 0, lootLocation.y(), 0).distance(lootLocation);
        if (world.equals(Bukkit.getWorld("world_the_end"))) {
            if (distanceFromSpawn > 10000.0d) {
                spellRarity = SpellTier.EPIC;
            } else {
                spellRarity = SpellTier.RARE;
            }
        } else if (world.equals(Bukkit.getWorld("world_nether"))) {
            if (Math.random() > 0.5) {
                spellRarity = SpellTier.UNCOMMON;
            } else {
                spellRarity = SpellTier.COMMON;
            }
        } else {
            if (y < 0) {
                spellRarity = SpellTier.UNCOMMON;
            } else {
                spellRarity = SpellTier.COMMON;
            }
        }
        if (spellRarity == null) return;
        logger.info(spellRarity.toString());

        Double dropChance = spellDropChances.get(spellRarity);
        if (dropChance == null || dropChance <= 0) return;

        double dice1 = Math.random();
        double dice2 = Math.random();
        double distance = Math.max(dice1, dice2) - Math.min(dice1, dice2);
        if (distance > dropChance) return;

        Set<Spell> spells = spellCache.get(spellRarity);
        if (spells == null || spells.isEmpty()) return;

        Spell spell = spells.stream()
                .skip((int) (Math.random() * spells.size()))
                .findFirst()
                .orElse(null);

        if (spell == null) return;

        ItemStack spellItem = createTornSpellBook(spell);
        event.getLoot().add(spellItem);
    }

    private ItemStack createTornSpellBook(Spell spell) {
        // TODO: Lore + Formatting
        ItemStack itemStack = ItemStack.of(Material.WRITTEN_BOOK, 1);
        BookMeta itemMeta = (BookMeta) itemStack.getItemMeta();
        itemMeta.addPages(Component.text(spell.getIncantation()));
        itemMeta.setAuthor("???");
        itemMeta.setCustomModelData(Math.round(tornSpellBookCustomModelData.getFirst()));
        itemMeta.setGeneration(BookMeta.Generation.TATTERED);
        itemMeta.itemName(Component.text("Torn Spellbook"));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

}
