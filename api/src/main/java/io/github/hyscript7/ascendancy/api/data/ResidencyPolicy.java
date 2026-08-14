package io.github.hyscript7.ascendancy.api.data;

/**
 * Declares when the entities of a {@link DataScope} are held in memory.
 * <p>
 * Accessors on {@link DataStore} are synchronous, so residency is what decides whether a read is a
 * map lookup or a trip to the disk.
 */
public enum ResidencyPolicy {
    /**
     * Every entity in the scope is loaded during startup and stays resident until shutdown.
     * <p>
     * This exists for scopes with <strong>no natural load trigger</strong>. Nobody "joins" a faction
     * the way a player joins a server, and "list all factions" has to work before anyone has asked
     * for a specific one — so the whole scope is simply resident, which makes enumeration trivially
     * correct.
     * <p>
     * Only appropriate for scopes that stay small. A scope with an entity per chunk must not use it.
     */
    EAGER,

    /**
     * Entities are loaded on first access and evicted by whoever owns their lifecycle — players on
     * quit, chunks on unload.
     * <p>
     * Reading a non-resident entity blocks on the storage backend. Callers that can afford to plan
     * ahead should use {@link DataStore#preload(DataKey)}.
     */
    LAZY
}
