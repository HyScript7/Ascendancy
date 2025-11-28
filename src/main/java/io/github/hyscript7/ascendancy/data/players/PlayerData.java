package io.github.hyscript7.ascendancy.data.players;

import lombok.Getter;

import java.util.UUID;

@Getter
public class PlayerData {
    private final UUID uuid;
    private boolean dirty;

    private String trueName;

    private long firstSeenTimestamp;
    private long lastSeenTimestamp;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.dirty = true;
    }

    public void markDirty() {
        this.dirty = true;
    }

    public void markClean() {
        this.dirty = false;
    }

    public void setTrueName(String trueName) {
        markDirty();
        this.trueName = trueName;
    }

    public void setFirstSeenTimestamp(long timestamp) {
        markDirty();
        this.firstSeenTimestamp = timestamp;
    }

    public void setFirstSeenTimestamp() {
        setFirstSeenTimestamp(System.currentTimeMillis());
    }

    public void setLastSeenTimestamp(long timestamp) {
        markDirty();
        this.lastSeenTimestamp = timestamp;
    }

    public void updateLastSeenTimestamp() {
        setLastSeenTimestamp(System.currentTimeMillis());
    }
}
