package io.github.hyscript7.ascendancy.api.data.value;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A {@link DataValue} holding named entries — the workhorse for anything shaped like a record.
 * <p>
 * Insertion order is preserved so that hand-inspecting a saved file is not an exercise in scrolling.
 * Equality remains order-independent, as {@link Map} equality always is.
 * <p>
 * Keys beginning with {@code $} are <strong>reserved for storage backends</strong> and rejected. The
 * JSON backend needs an unambiguous sentinel for values JSON cannot represent (see
 * {@link DataBytes}), and stealing a prefix up front is cheaper than escaping forever.
 *
 * @param entries The wrapped entries, unmodifiable and never null
 */
public record DataMap(Map<String, DataValue> entries) implements DataValue {
    /**
     * @throws IllegalArgumentException If the map, any key, or any value is null, if a key is blank,
     *                                  or if a key begins with the reserved {@code $} prefix
     */
    public DataMap {
        if (entries == null) {
            throw new IllegalArgumentException("DataMap entries cannot be null");
        }
        Map<String, DataValue> copy = new LinkedHashMap<>();
        for (Map.Entry<String, DataValue> entry : entries.entrySet()) {
            String key = entry.getKey();
            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException("DataMap keys cannot be null or blank");
            }
            if (key.startsWith("$")) {
                throw new IllegalArgumentException(
                        "DataMap keys cannot begin with '$', which is reserved for storage backends: " + key);
            }
            if (entry.getValue() == null) {
                throw new IllegalArgumentException("DataMap value for key \"" + key + "\" cannot be null");
            }
            copy.put(key, entry.getValue());
        }
        entries = Collections.unmodifiableMap(copy);
    }

    /**
     * @return An empty map
     */
    public static DataMap empty() {
        return new DataMap(Map.of());
    }

    /**
     * @return A fresh builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * @param key The key to look up
     * @return True if the key is present
     */
    public boolean contains(String key) {
        return entries.containsKey(key);
    }

    /**
     * Looks up a raw value.
     *
     * @param key The key to look up
     * @return The value, or empty if absent
     */
    public Optional<DataValue> get(String key) {
        return Optional.ofNullable(entries.get(key));
    }

    /**
     * Looks up a nested map.
     *
     * @param key The key to look up
     * @throws DataCodecException If the key is present but holds another type
     * @return The nested map, or empty if absent
     */
    public Optional<DataMap> getMap(String key) {
        return expect(key, DataMap.class);
    }

    /**
     * Looks up a nested list.
     *
     * @param key The key to look up
     * @throws DataCodecException If the key is present but holds another type
     * @return The nested list, or empty if absent
     */
    public Optional<DataList> getList(String key) {
        return expect(key, DataList.class);
    }

    /**
     * Looks up a string.
     *
     * @param key The key to look up
     * @throws DataCodecException If the key is present but holds another type
     * @return The string, or empty if absent
     */
    public Optional<String> getString(String key) {
        return expect(key, DataString.class).map(DataString::value);
    }

    /**
     * Looks up a whole number.
     *
     * @param key The key to look up
     * @throws DataCodecException If the key is present but holds another type
     * @return The number, or empty if absent
     */
    public Optional<Long> getInteger(String key) {
        return expect(key, DataInteger.class).map(DataInteger::value);
    }

    /**
     * Looks up a fractional number. Whole numbers are widened, since a value that happens to have
     * been written as {@code 0} should still read back as a decimal {@code 0.0}.
     *
     * @param key The key to look up
     * @throws DataCodecException If the key is present but holds a non-numeric type
     * @return The number, or empty if absent
     */
    public Optional<Double> getDecimal(String key) {
        DataValue value = entries.get(key);
        if (value == null) {
            return Optional.empty();
        }
        return switch (value) {
            case DataDecimal decimal -> Optional.of(decimal.value());
            case DataInteger integer -> Optional.of((double) integer.value());
            default -> throw typeMismatch(key, "DataDecimal", value);
        };
    }

    /**
     * Looks up a boolean.
     *
     * @param key The key to look up
     * @throws DataCodecException If the key is present but holds another type
     * @return The boolean, or empty if absent
     */
    public Optional<Boolean> getBoolean(String key) {
        return expect(key, DataBoolean.class).map(DataBoolean::value);
    }

    /**
     * Looks up a byte array.
     *
     * @param key The key to look up
     * @throws DataCodecException If the key is present but holds another type
     * @return A copy of the bytes, or empty if absent
     */
    public Optional<byte[]> getBytes(String key) {
        return expect(key, DataBytes.class).map(DataBytes::value);
    }

    /**
     * Looks a key up and asserts its type.
     * <p>
     * A present-but-wrong-typed key throws rather than reading as absent. Silently falling back to a
     * default would turn a schema mistake into data loss on the next save.
     *
     * @param <T>      The expected value type
     * @param key      The key to look up
     * @param expected The expected {@link DataValue} implementation
     * @throws DataCodecException If the key is present but holds another type
     * @return The typed value, or empty if absent
     */
    private <T extends DataValue> Optional<T> expect(String key, Class<T> expected) {
        DataValue value = entries.get(key);
        if (value == null) {
            return Optional.empty();
        }
        if (!expected.isInstance(value)) {
            throw typeMismatch(key, expected.getSimpleName(), value);
        }
        return Optional.of(expected.cast(value));
    }

    private static DataCodecException typeMismatch(String key, String expected, DataValue actual) {
        return new DataCodecException("Expected " + expected + " at key \"" + key + "\", but found "
                + actual.getClass().getSimpleName());
    }

    /**
     * A mutable builder for {@link DataMap}, since assembling a nested {@link Map} literal by hand is
     * miserable.
     */
    public static final class Builder {
        private final Map<String, DataValue> entries = new LinkedHashMap<>();

        private Builder() {}

        /**
         * @param key   The key to set
         * @param value The value to set
         * @return This builder
         */
        public Builder put(String key, DataValue value) {
            entries.put(key, value);
            return this;
        }

        /**
         * @param key   The key to set
         * @param value The value to set
         * @return This builder
         */
        public Builder put(String key, String value) {
            return put(key, DataValue.of(value));
        }

        /**
         * @param key   The key to set
         * @param value The value to set
         * @return This builder
         */
        public Builder put(String key, long value) {
            return put(key, DataValue.of(value));
        }

        /**
         * @param key   The key to set
         * @param value The value to set
         * @return This builder
         */
        public Builder put(String key, double value) {
            return put(key, DataValue.of(value));
        }

        /**
         * @param key   The key to set
         * @param value The value to set
         * @return This builder
         */
        public Builder put(String key, boolean value) {
            return put(key, DataValue.of(value));
        }

        /**
         * @param key   The key to set
         * @param value The value to set
         * @return This builder
         */
        public Builder put(String key, byte[] value) {
            return put(key, DataValue.of(value));
        }

        /**
         * Sets a key only if the value is present, which saves callers an {@code if} per optional
         * field.
         *
         * @param key   The key to set
         * @param value The value to set if present
         * @return This builder
         */
        public Builder putIfPresent(String key, Optional<? extends DataValue> value) {
            value.ifPresent(present -> put(key, present));
            return this;
        }

        /**
         * @throws IllegalArgumentException If any accumulated key or value is invalid
         * @return The assembled map
         */
        public DataMap build() {
            return new DataMap(entries);
        }
    }
}
