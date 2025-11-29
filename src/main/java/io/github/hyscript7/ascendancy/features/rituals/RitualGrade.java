package io.github.hyscript7.ascendancy.features.rituals;

import lombok.Getter;
import org.bukkit.Material;

import java.util.Set;

public enum RitualGrade {
    BASIC(Material.AMETHYST_SHARD, Material.ECHO_SHARD, Material.WHITE_BANNER), // White banner because of pillager banners and faction creation
    INTERMEDIATE(Material.CREEPER_HEAD, Material.ZOMBIE_HEAD, Material.SKELETON_SKULL, Material.PRISMARINE_SHARD, Material.PRISMARINE_CRYSTALS, Material.ENDER_EYE),
    ADVANCED(Material.PLAYER_HEAD, Material.NETHERITE_INGOT),
    MASTER(Material.DRAGON_HEAD, Material.TOTEM_OF_UNDYING, Material.PIGLIN_HEAD, Material.WITHER_SKELETON_SKULL),
    MYTHIC(Material.NETHER_STAR, Material.DRAGON_EGG);

    @Getter
    private final Set<Material> catalysts;

    RitualGrade(Material ...catalystType) {
        this.catalysts = Set.of(catalystType);
    }

    /**
     * Returns the ritual grade corresponding to the given catalyst material.
     * @param catalyst The material
     * @return A ritual grade if the material is a catalyst, otherwise null.
     */
    public static RitualGrade fromCatalyst(Material catalyst) {
        for (RitualGrade grade : RitualGrade.values()) {
            if (grade.catalysts.contains(catalyst)) {
                return grade;
            }
        }
        return null;
    }
}
