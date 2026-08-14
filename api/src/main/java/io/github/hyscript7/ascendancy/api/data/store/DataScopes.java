package io.github.hyscript7.ascendancy.api.data.store;

import io.github.hyscript7.ascendancy.api.registry.Identifier;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * The scopes Core registers itself, and the key derivation that goes with them.
 * <p>
 * These live in {@code :api} so that every consumer derives a chunk key the same way. If each
 * feature invented its own format, two of them would eventually disagree about the same chunk and
 * nobody would notice until the data was already split in half.
 * <p>
 * This is key derivation only — the ergonomic API for reading a player's stats is a separate feature
 * built on top of this one.
 */
public final class DataScopes {
    /** The namespace Core registers its own scopes under. */
    public static final String NAMESPACE = "ascendancy";

    /** The id of the single entity in the {@link #GLOBAL} scope. */
    private static final String SERVER_ID = "server";

    /**
     * Server-wide data belonging to no world at all — which factions exist, which teams have been
     * founded, and anything else that would be wrong to lose when a dimension unloads.
     * <p>
     * {@link ResidencyPolicy#EAGER}, because there is nothing to trigger its load: nobody joins the
     * server-wide entity the way a player joins a server.
     * <p>
     * Not to be confused with {@link #WORLD}, which is keyed per world and so has a separate entity
     * for the overworld, the nether and the end. Anything stored there would be invisible from the
     * other two.
     */
    public static final DataScope GLOBAL = DataScope.eager(Identifier.of(NAMESPACE, "global"));

    /**
     * One entity per player, keyed by player UUID.
     * <p>
     * {@link ResidencyPolicy#LAZY}: loaded on join, unloaded on quit.
     */
    public static final DataScope PLAYER = DataScope.lazy(Identifier.of(NAMESPACE, "player"));

    /**
     * One entity per chunk, keyed by {@code <worldUuid>_<x>_<z>}.
     * <p>
     * {@link ResidencyPolicy#LAZY}: loaded on chunk load, unloaded on chunk unload. Emphatically not
     * eager — a populated world has rather a lot of chunks.
     */
    public static final DataScope CHUNK = DataScope.lazy(Identifier.of(NAMESPACE, "chunk"));

    /**
     * One entity per world, keyed by world UUID.
     * <p>
     * {@link ResidencyPolicy#EAGER}: there are few enough worlds that keeping them all resident is
     * free.
     */
    public static final DataScope WORLD = DataScope.eager(Identifier.of(NAMESPACE, "world"));

    private DataScopes() {}

    /**
     * @return Every scope Core registers, in registration order
     */
    public static DataScope[] all() {
        return new DataScope[] {PLAYER, CHUNK, WORLD, GLOBAL};
    }

    /**
     * The key for the single server-wide entity.
     * <p>
     * Suits a bounded amount of server state — a component listing the factions that exist, say.
     * For a genuinely large or unbounded collection, register an eager scope of your own instead and
     * give each faction its own entity:
     *
     * <pre>{@code
     * DataScope factions = DataScope.eager(Identifier.of(plugin, "faction"));
     * persistence.scopes().register(factions);
     * persistence.store().get(factions.key(factionId)).set(MEMBERS, members);
     * }</pre>
     *
     * That keeps each faction independently readable and writable, rather than rewriting one large
     * component every time anybody joins a team.
     *
     * @return The key addressing server-wide data
     */
    public static DataKey global() {
        return GLOBAL.key(SERVER_ID);
    }

    /**
     * Derives the key for a player.
     *
     * @param player The player
     * @throws IllegalArgumentException If the player is null
     * @return The key addressing that player's data
     */
    public static DataKey of(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        return PLAYER.key(player.getUniqueId());
    }

    /**
     * Derives the key for a chunk.
     * <p>
     * Keyed on the world's UUID rather than its name, because world names are user-supplied and end
     * up in a file path.
     *
     * @param chunk The chunk
     * @throws IllegalArgumentException If the chunk is null
     * @return The key addressing that chunk's data
     */
    public static DataKey of(Chunk chunk) {
        if (chunk == null) {
            throw new IllegalArgumentException("Chunk cannot be null");
        }
        return chunkKey(chunk.getWorld(), chunk.getX(), chunk.getZ());
    }

    /**
     * Derives the key for a chunk by coordinate, for callers holding coordinates rather than a
     * loaded {@link Chunk}.
     *
     * @param world The world containing the chunk
     * @param x     The chunk's x coordinate
     * @param z     The chunk's z coordinate
     * @throws IllegalArgumentException If the world is null
     * @return The key addressing that chunk's data
     */
    public static DataKey chunkKey(World world, int x, int z) {
        if (world == null) {
            throw new IllegalArgumentException("World cannot be null");
        }
        // Negative coordinates would otherwise contribute a '-', which DataKey happens to allow.
        return CHUNK.key(world.getUID() + "_" + x + "_" + z);
    }

    /**
     * Derives the key for a world.
     *
     * @param world The world
     * @throws IllegalArgumentException If the world is null
     * @return The key addressing that world's data
     */
    public static DataKey of(World world) {
        if (world == null) {
            throw new IllegalArgumentException("World cannot be null");
        }
        return WORLD.key(world.getUID());
    }
}
