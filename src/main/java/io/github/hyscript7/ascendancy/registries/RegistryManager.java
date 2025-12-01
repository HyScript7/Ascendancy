package io.github.hyscript7.ascendancy.registries;

import io.github.hyscript7.ascendancy.AlreadyInitializedException;
import io.github.hyscript7.ascendancy.builtins.magic.arcanas.VoidWalk;
import io.github.hyscript7.ascendancy.builtins.magic.spells.Fireball;
import io.github.hyscript7.ascendancy.builtins.rituals.instant.RitualOfDeathReturn;
import io.github.hyscript7.ascendancy.builtins.rituals.instant.RitualOfHomeComing;
import io.github.hyscript7.ascendancy.builtins.rituals.instant.RitualOfSoulSending;
import io.github.hyscript7.ascendancy.builtins.rituals.lasting.RitualOfVoidRevealing;
import io.github.hyscript7.ascendancy.features.innate.names.InnateCommand;
import io.github.hyscript7.ascendancy.builtins.innate.commands.other.Bestow;
import io.github.hyscript7.ascendancy.builtins.innate.commands.self.GetOwnName;
import io.github.hyscript7.ascendancy.features.magic.Spell;
import io.github.hyscript7.ascendancy.features.rituals.Ritual;
import io.github.hyscript7.ascendancy.builtins.rituals.instant.RitualOfLevitation;
import lombok.Getter;

/**
 * A singleton for managing and initializing all registries in the plugin.
 */
@Getter
public class RegistryManager {
    private static RegistryManager instance;

    private final Registry<InnateCommand> innateCommandRegistry;
    private final Registry<Ritual> ritualRegistry;
    private final Registry<Spell> spellRegistry;

    private boolean initialized;

    private RegistryManager() {
        this.initialized = false;
        this.innateCommandRegistry = new Registry<>("InnateCommands");
        this.ritualRegistry = new Registry<>("Rituals");
        this.spellRegistry= new Registry<>("Spells");
    }

    public static RegistryManager getInstance() {
        if (instance == null) {
            instance = new RegistryManager();
        }
        return instance;
    }

    public void initialize() {
        if (initialized) {
            throw new AlreadyInitializedException("The RegistryManager has already been initialized!");
        }

        // Add all instances
        registerInnateCommands();
        registerRituals();
        registerSpells();

        // Lock all registries

        initialized = true;
    }

    private void registerInnateCommands() {
        innateCommandRegistry.register(new GetOwnName());
        innateCommandRegistry.register(new Bestow());
        // TODO: Add commands
    }

    private void registerRituals() {
        ritualRegistry.register(new RitualOfLevitation());
        ritualRegistry.register(new RitualOfHomeComing());
        ritualRegistry.register(new RitualOfDeathReturn());
        ritualRegistry.register(new RitualOfSoulSending());
        ritualRegistry.register(new RitualOfVoidRevealing());
        // TODO: Add rituals
    }

    private void registerSpells() {
        spellRegistry.register(new Fireball());
        spellRegistry.register(new VoidWalk());
        // TODO: Add spells
    }
}
