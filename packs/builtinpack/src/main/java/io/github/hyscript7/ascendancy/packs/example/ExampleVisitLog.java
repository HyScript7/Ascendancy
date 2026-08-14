package io.github.hyscript7.ascendancy.packs.example;

import io.github.hyscript7.ascendancy.api.data.component.ComponentType;
import io.github.hyscript7.ascendancy.api.data.value.DataMap;
import io.github.hyscript7.ascendancy.api.registry.Identifier;

/**
 * How many times a player has joined, and when they were last seen.
 * <p>
 * Exists purely to demonstrate the persistence flow end to end — it is not a designed feature, and
 * deleting it costs nothing. Real player stats are specified in {@code docs/Mechanics and
 * Systems/Progression.md} and belong to Core, not to a pack.
 * <p>
 * A component value is a <strong>record</strong> for a reason: the store notices changes when you
 * call {@code set}, so a value mutated in place behind its back is never written. Read it, build a
 * new one, set it back.
 *
 * @param visits             How many times the player has joined, never negative
 * @param lastSeenEpochMillis When the player last joined, or 0 if they never have
 */
public record ExampleVisitLog(int visits, long lastSeenEpochMillis) {
    /**
     * @throws IllegalArgumentException If the visit count is negative
     */
    public ExampleVisitLog {
        if (visits < 0) {
            throw new IllegalArgumentException("Visit count cannot be negative, got " + visits);
        }
    }

    /**
     * @return The value used for a player who has never been seen before
     */
    public static ExampleVisitLog first() {
        return new ExampleVisitLog(0, 0L);
    }

    /**
     * Describes how this record is stored: its default, and the codec both ways.
     * <p>
     * Lives here rather than in the plugin class so the codec sits beside the shape it encodes —
     * change a field and the thing that has to change with it is on the next screen, not in another
     * file.
     * <p>
     * The identifier is a parameter because its namespace must be the owning plugin's name, and there
     * is no plugin instance until Bukkit builds one. If this shape ever changes in a way that stops
     * the decoder reading data already on disk, add {@code .version(2)} and a {@code .migrator(...)}
     * via {@link ComponentType#builder} rather than editing the decoder in place.
     *
     * @param identifier Names the component, built with {@code Identifier.of(plugin, path)}
     * @throws IllegalArgumentException If the identifier is null
     * @return The component type, ready to register
     */
    public static ComponentType<ExampleVisitLog> componentType(Identifier identifier) {
        return ComponentType.ofMap(
                identifier,
                ExampleVisitLog.class,
                ExampleVisitLog::first,
                value -> DataMap.builder()
                        .put("visits", value.visits())
                        .put("last_seen", value.lastSeenEpochMillis())
                        .build(),
                // orElse rather than orElseThrow: a field added in a later version is simply absent
                // in data already on disk, and a sensible default beats a migration for that case.
                data -> new ExampleVisitLog(
                        data.getInteger("visits").orElse(0L).intValue(),
                        data.getInteger("last_seen").orElse(0L)));
    }

    /**
     * @param now The current time, in epoch milliseconds
     * @return A new log with the visit counted, since records do not change in place
     */
    public ExampleVisitLog withVisitAt(long now) {
        return new ExampleVisitLog(visits + 1, now);
    }

    /**
     * @return True if this player has been seen before
     */
    public boolean returning() {
        return visits > 0;
    }
}
