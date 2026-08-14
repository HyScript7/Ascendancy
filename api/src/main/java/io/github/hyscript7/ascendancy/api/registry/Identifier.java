package io.github.hyscript7.ascendancy.api.registry;

import org.bukkit.plugin.Plugin;

/**
 * An Ascendancy Identifier used for data storage and registries.
 *
 * Will throw IllegalArgumentException when instantiating with:
 * - A blank namespace
 * - A blank path
 * - A null namespace
 * - A null path
 * - A path containing the ':' symbol
 * - A namespace containing the ':' symbol
 */
public record Identifier(String namespace, String path) {
    public Identifier {
        if (namespace == null || namespace.isBlank()) {
            throw new IllegalArgumentException("Namespace cannot be empty");
        }
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Path cannot be empty");
        }
        if (!namespace.replaceAll("[^:]", "").isEmpty()) {
            throw new IllegalArgumentException("The namespace cannot contain any colon symbols (':')");
        }
        if (!path.replaceAll("[^:]", "").isEmpty()) {
            throw new IllegalArgumentException("The path cannot contain any colon symbols (':')");
        }
    }

    /**
     * A shorthand for creating an identifier using the given namespace and path.
     *
     * @param namespace The namespace that owns the identifier
     * @param path      The path
     * @throws IllegalArgumentException When validation of the namespace or path fail
     * @return The identifier
     */
    public static Identifier of(String namespace, String path) {
        return new Identifier(namespace, path);
    }

    /**
     * Shorthand for creating an identifier of path using the plugin as the
     * namespace.
     *
     * @param plugin The plugin that owns this identifier (used as namespace)
     * @param path   The path
     * @throws IllegalArgumentException When (a) the provided plugin is null, (b) validation of the namespace or path fail
     * @return The identifier
     */
    public static Identifier of(Plugin plugin, String path) throws IllegalArgumentException {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        return new Identifier(plugin.getName(), path);
    }

    /**
     * Parses a stringified Identifier into it's corresponding namespace and path
     * using a colon (:) as the separator.
     *
     * @param value A stringified identifier in the format of [namespace]:[path]
     * @throws IllegalArgumentException When (a) the identifier is null or blank,
     *                                  (b) either part of the identifier is blank,
     *                                  (c) if there are multiple colons to use as
     *                                  separators, (d) validation of the namespace
     *                                  or path fail.
     * @return An instance of Identifier representing the given value
     */
    public static Identifier parse(String value) throws IllegalArgumentException {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Identifier cannot be null/blank");
        }

        String[] split = value.split(":", 2);
        if (split.length != 2) {
            throw new IllegalArgumentException("Invalid identifier \"" + value
                    + "\", must contain exactly one separator in the form of a colon (':')");
        }

        if (split[0].isBlank() || split[1].isBlank()) {
            throw new IllegalArgumentException("Neither part of the identifier can be blank");
        }

        return new Identifier(split[0], split[1]);
    }

    /**
     * Stringifies an identifier following the [namespace]:[path] schema.
     * Inverse of {@link #parse(String)}.
     */
    @Override
    public String toString() {
        return namespace + ":" + path;
    }
}
