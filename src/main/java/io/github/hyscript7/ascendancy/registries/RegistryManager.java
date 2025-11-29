package io.github.hyscript7.ascendancy.registries;

import io.github.hyscript7.ascendancy.AlreadyInitializedException;
import io.github.hyscript7.ascendancy.features.innate.names.InnateCommand;
import io.github.hyscript7.ascendancy.features.innate.names.commands.other.Bestow;
import io.github.hyscript7.ascendancy.features.innate.names.commands.self.GetOwnName;
import io.github.hyscript7.ascendancy.features.rituals.Ritual;
import io.github.hyscript7.ascendancy.features.rituals.recipes.instant.RitualOfLevitation;
import lombok.Getter;

/**
 * A singleton for managing and initializing all registries in the plugin.
 */
@Getter
public class RegistryManager {
    private static RegistryManager instance;

    private final Registry<InnateCommand> innateCommandRegistry;
    private final Registry<Ritual> ritualRegistry;

    private boolean initialized;

    private RegistryManager() {
        this.initialized = false;
        this.innateCommandRegistry = new Registry<>("InnateCommands");
        this.ritualRegistry = new Registry<>("Rituals");
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
        // TODO: Add rituals
    }
}
