package io.github.hyscript7.ascendancy.registries;

import io.github.hyscript7.ascendancy.AlreadyInitializedException;
import io.github.hyscript7.ascendancy.features.innate.names.InnateCommand;
import io.github.hyscript7.ascendancy.features.innate.names.commands.other.Bestow;
import io.github.hyscript7.ascendancy.features.innate.names.commands.self.GetOwnName;
import lombok.Getter;

/**
 * A singleton for managing and initializing all registries in the plugin.
 */
@Getter
public class RegistryManager {
    private static RegistryManager instance;

    private final Registry<InnateCommand> innateCommandRegistry;

    private boolean initialized;

    private RegistryManager() {
        this.initialized = false;
        this.innateCommandRegistry = new Registry<>("InnateCommands");
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

        // Lock all registries

        initialized = true;
    }

    private void registerInnateCommands() {
        innateCommandRegistry.register(new GetOwnName());
        innateCommandRegistry.register(new Bestow());
        // TODO: Add commands
    }
}
