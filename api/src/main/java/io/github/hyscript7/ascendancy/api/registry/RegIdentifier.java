package io.github.hyscript7.ascendancy.api.registry;

public record RegIdentifier(String namespace, String path)
{
    public RegIdentifier {
        if (namespace == null || namespace.isBlank()) {
            throw new IllegalArgumentException("Namespace cannot be empty");
        }
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Path cannot be empty");
        }
    }

    public static RegIdentifier of(String namespace, String path) {
        return new RegIdentifier(namespace, path);
    }

    public static RegIdentifier of(org.bukkit.plugin.Plugin plugin, String path) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null");
        }
        return new RegIdentifier(plugin.getName(), path);
    }
    /**
    *Parses a stringified id into 2 parts using a colon as a separator [namespace]:[path]
    *Then it returns them as a an instance of this class
    */
    public static RegIdentifier parse(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Identifier cannot be null/blank");
        }

        String[] split = value.split(":", 2);
        if (split.length != 2) {
            throw new IllegalArgumentException("Invalid identifier format: " + value);
        }

        return new RegIdentifier(split[0], split[1]);
    }
    /**
    * This is the reverse of the parse method, where it combines the 2 id segments together
    */
    @Override
    public String toString() {
        return namespace + ":" + path;
    }
}