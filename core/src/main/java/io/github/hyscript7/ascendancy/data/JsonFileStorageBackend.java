package io.github.hyscript7.ascendancy.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import io.github.hyscript7.ascendancy.api.data.DataKey;
import io.github.hyscript7.ascendancy.api.data.DataStorageException;
import io.github.hyscript7.ascendancy.api.data.DataValue;
import io.github.hyscript7.ascendancy.api.registry.Identifier;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import lombok.extern.slf4j.Slf4j;

/**
 * Stores each entity as one JSON file, laid out as
 * {@code <root>/<scope namespace>/<scope path>/<entity id>.json}.
 * <p>
 * The scope {@link Identifier} doubles as the directory path, which its {@code namespace:path} shape
 * happens to suit perfectly.
 * <p>
 * Chosen as the default because it needs no setup from server owners and can be read — and repaired
 * — in a text editor at three in the morning, which is when persistence problems are traditionally
 * discovered.
 */
@Slf4j
public class JsonFileStorageBackend implements StorageBackend {
    /**
     * Bumped only if the file layout changes in a way a reader must know about. Written under a
     * reserved {@code $} key, which {@link io.github.hyscript7.ascendancy.api.data.DataMap} forbids
     * callers from using, so it can never collide with a component identifier.
     */
    private static final String VERSION_KEY = "$version";

    private static final int CURRENT_VERSION = 1;

    private static final String FILE_EXTENSION = ".json";

    private final Path root;
    private final Gson gson;

    /**
     * @param root The directory to store data under, created on demand
     * @throws IllegalArgumentException If the root is null
     */
    public JsonFileStorageBackend(Path root) {
        if (root == null) {
            throw new IllegalArgumentException("Storage root cannot be null");
        }
        this.root = root;
        this.gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    }

    @Override
    public Optional<Map<Identifier, DataValue>> read(DataKey key) {
        Path file = fileFor(key);
        if (!Files.isRegularFile(file)) {
            return Optional.empty();
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonElement parsed = JsonParser.parseReader(reader);
            if (!parsed.isJsonObject()) {
                throw new DataStorageException("Expected a JSON object at the root of " + file);
            }
            Map<Identifier, DataValue> components = new LinkedHashMap<>();
            for (Map.Entry<String, JsonElement> entry : parsed.getAsJsonObject().entrySet()) {
                if (entry.getKey().startsWith("$")) {
                    continue;
                }
                components.put(Identifier.parse(entry.getKey()), JsonDataValueCodec.decode(entry.getValue()));
            }
            return Optional.of(components);
        } catch (IOException | JsonParseException | IllegalArgumentException exception) {
            throw new DataStorageException("Failed to read entity " + key + " from " + file, exception);
        }
    }

    @Override
    public void write(DataKey key, Map<Identifier, DataValue> components) {
        Path file = fileFor(key);
        JsonObject document = new JsonObject();
        document.addProperty(VERSION_KEY, CURRENT_VERSION);
        // Sorted by identifier so that repeated saves of unchanged data produce identical files.
        Map<String, DataValue> sorted = new TreeMap<>();
        components.forEach((identifier, value) -> sorted.put(identifier.toString(), value));
        sorted.forEach((identifier, value) -> document.add(identifier, JsonDataValueCodec.encode(value)));

        // Written beside the target rather than into a temp directory, so the atomic move below
        // cannot be defeated by the two paths landing on different filesystems.
        Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                gson.toJson(document, writer);
            }
            moveIntoPlace(temporary, file);
        } catch (IOException exception) {
            throw new DataStorageException("Failed to write entity " + key + " to " + file, exception);
        } finally {
            try {
                Files.deleteIfExists(temporary);
            } catch (IOException exception) {
                log.warn("Could not clean up temporary file {}", temporary, exception);
            }
        }
    }

    /**
     * Replaces the target with the temporary file in one step, so a server killed mid-write leaves
     * the previous version intact rather than a half-written file that fails to parse on boot.
     *
     * @param temporary The fully written temporary file
     * @param target    The file to replace
     * @throws IOException If the move fails
     */
    private void moveIntoPlace(Path temporary, Path target) throws IOException {
        try {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException exception) {
            // Some filesystems (notably a few network mounts) refuse. A non-atomic replace is still
            // better than writing the target in place.
            log.debug("Atomic move unsupported for {}, falling back to a plain replace", target);
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @Override
    public boolean delete(DataKey key) {
        Path file = fileFor(key);
        try {
            return Files.deleteIfExists(file);
        } catch (IOException exception) {
            throw new DataStorageException("Failed to delete entity " + key + " at " + file, exception);
        }
    }

    @Override
    public boolean exists(DataKey key) {
        return Files.isRegularFile(fileFor(key));
    }

    @Override
    public Collection<DataKey> keys(Identifier scope) {
        Path directory = directoryFor(scope);
        if (!Files.isDirectory(directory)) {
            return List.of();
        }
        Collection<DataKey> keys = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, "*" + FILE_EXTENSION)) {
            for (Path file : stream) {
                String name = file.getFileName().toString();
                String id = name.substring(0, name.length() - FILE_EXTENSION.length());
                try {
                    keys.add(DataKey.of(scope, id));
                } catch (IllegalArgumentException exception) {
                    // Somebody dropped a file in here by hand. Skipping beats aborting the scope.
                    log.warn("Ignoring file with an invalid entity id in scope {}: {}", scope, name);
                }
            }
        } catch (IOException exception) {
            throw new DataStorageException("Failed to enumerate scope " + scope + " at " + directory, exception);
        }
        return keys;
    }

    @Override
    public void close() {
        // Nothing to release; every write closes its own handle. Present for backends that do.
    }

    /**
     * @param key The entity to locate
     * @return The file backing that entity
     */
    private Path fileFor(DataKey key) {
        return directoryFor(key.scope()).resolve(key.id() + FILE_EXTENSION);
    }

    /**
     * @param scope The scope to locate
     * @return The directory holding that scope's entities
     */
    private Path directoryFor(Identifier scope) {
        return root.resolve(scope.namespace()).resolve(scope.path());
    }
}
