package io.github.hyscript7.ascendancy.data.players.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.github.hyscript7.ascendancy.AscendancyPlugin;
import io.github.hyscript7.ascendancy.data.players.PlayerData;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;

public class JsonPlayerDataStorage implements PlayerDataStorage {
    private final File dataFolder;
    private final Gson gson;

    public JsonPlayerDataStorage(AscendancyPlugin plugin) {
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    public PlayerData load(UUID uuid) throws IOException {
        File file = getPlayerFile(uuid);
        if (!file.exists()) {
            return null;
        }

        try (Reader reader = new FileReader(file)) {
            JsonObject json = gson.fromJson(reader, JsonObject.class);
            return deserialize(json);
        }
    }

    @Override
    public void save(PlayerData data) throws IOException {
        File file = getPlayerFile(data.getUuid());

        JsonObject json = serialize(data);

        try (Writer writer = new FileWriter(file)) {
            gson.toJson(json, writer);
        }
    }

    @Override
    public boolean exists(UUID uuid) {
        return getPlayerFile(uuid).exists();
    }

    @Override
    public void delete(UUID uuid) throws IOException {
        File file = getPlayerFile(uuid);
        if (file.exists()) {
            Files.delete(file.toPath());
        }
    }

    @Override
    public List<PlayerData> loadAll() {
        var files = dataFolder.listFiles();
        if (files == null) {
            AscendancyPlugin.getInstance().getLogger().log(Level.WARNING, "List files on player data returned null, is the player data path correctly pointing to a directory?");
            return Collections.emptyList();
        }
        return Arrays.stream(files).map(
                file -> {
                    try (Reader reader = new FileReader(file)) {
                        JsonObject json = gson.fromJson(reader, JsonObject.class);
                        return deserialize(json);
                    } catch (IOException e) {
                        return null;
                    }
                }
        ).filter(Objects::nonNull).toList();
    }

    private File getPlayerFile(UUID uuid) {
        return new File(dataFolder, uuid.toString() + ".json");
    }

    private JsonObject serialize(PlayerData data) {
        JsonObject json = new JsonObject();

        json.addProperty("uuid", data.getUuid().toString());
        json.addProperty("firstSeenTimestamp", data.getFirstSeenTimestamp());
        json.addProperty("lastSeenTimestamp", data.getLastSeenTimestamp());

        return json;
    }

    private PlayerData deserialize(JsonObject json) {
        UUID uuid = UUID.fromString(json.get("uuid").getAsString());
        PlayerData data = new PlayerData(uuid);

        if (json.has("firstSeenTimestamp")) {
            data.setFirstSeenTimestamp(json.get("firstSeenTimestamp").getAsLong());
        }

        if (json.has("lastSeenTimestamp")) {
            data.setLastSeenTimestamp(json.get("lastSeenTimestamp").getAsLong());
        }

        data.markClean(); // Clean, since we just loaded it
        return data;
    }
}
