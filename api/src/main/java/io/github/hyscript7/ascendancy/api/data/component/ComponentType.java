package io.github.hyscript7.ascendancy.api.data.component;

import io.github.hyscript7.ascendancy.api.data.Persistence;
import io.github.hyscript7.ascendancy.api.data.value.DataCodecException;
import io.github.hyscript7.ascendancy.api.data.value.DataMap;
import io.github.hyscript7.ascendancy.api.data.value.DataValue;
import io.github.hyscript7.ascendancy.api.registry.Identifiable;
import io.github.hyscript7.ascendancy.api.registry.Identifier;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Describes one kind of data that can be attached to an entity: its identifier, its value class, its
 * default, its version, and how it converts to and from {@link DataValue}.
 * <p>
 * <strong>Component values must be immutable</strong>, ideally records. The store tracks changes at
 * {@link ComponentHolder#set(ComponentType, Object)}, so a value mutated in place behind its back
 * will not be saved.
 * <p>
 * Types are registered in {@link Persistence#componentTypes()}. The registry's job here is
 * <em>collision detection</em> — stopping two packs silently sharing one identifier — and not codec
 * lookup. Decoding never consults it, which is exactly what lets unknown components survive a
 * round-trip untouched.
 *
 * @param <T> The value type carried by this component
 */
public interface ComponentType<T> extends Identifiable {
    /**
     * @return The identifier naming this component
     */
    @Override
    Identifier getIdentifier();

    /**
     * @return The class of value this component carries
     */
    Class<T> valueType();

    /**
     * The value returned for an entity that has never had this component set.
     * <p>
     * Every component has one, so consumers never handle {@link java.util.Optional} for a component
     * they own — which matches how the design documents specify stats, each with a stated default.
     *
     * @throws DataCodecException If the default cannot be produced
     * @return A fresh default value, never null
     */
    T defaultValue() throws DataCodecException;

    /**
     * The current shape of this component's stored data, written alongside it and used to decide
     * whether data read back needs migrating.
     * <p>
     * Starts at 1. Bump it whenever a change would stop the current {@link #decode(DataValue)} from
     * reading data already on disk, and supply a {@link #migrator()} that brings the old shape
     * forward. Do not bump it for changes that read back fine — a new optional field with a
     * sensible default needs no migration.
     *
     * @return The current version, at least 1
     */
    default int version() {
        return 1;
    }

    /**
     * Brings stored data written by an older {@link #version()} up to the current shape.
     * <p>
     * The default refuses, which is the honest answer for a component that has never declared a
     * migration: silently accepting mismatched data would decode garbage or throw somewhere less
     * informative.
     *
     * @return The migrator, or null if this component has no migration path
     */
    default ComponentMigrator migrator() {
        return null;
    }

    /**
     * Converts a value into its persistable form.
     *
     * @param value The value to encode
     * @throws DataCodecException If the value cannot be represented
     * @return The encoded form
     */
    DataValue encode(T value) throws DataCodecException;

    /**
     * Reconstructs a value from its persistable form.
     * <p>
     * Always receives data in the current {@link #version()}'s shape — anything older has already
     * been through {@link #migrator()} by the time it arrives here.
     *
     * @param data The encoded form, as previously produced by {@link #encode(Object)}
     * @throws DataCodecException If the data is malformed or of the wrong shape
     * @return The decoded value
     */
    T decode(DataValue data) throws DataCodecException;

    /**
     * Creates a version 1 component type from functions, which spares packs writing a class per
     * component. Use {@link #builder(Identifier, Class)} when a component needs a version or a
     * migration.
     *
     * @param <T>          The value type
     * @param identifier   Names the component
     * @param valueType    The class of value carried
     * @param defaultValue Supplies the value used when the component is absent
     * @param encoder      Converts a value into its persistable form
     * @param decoder      Reconstructs a value from its persistable form
     * @throws IllegalArgumentException If any argument is null
     * @return The component type
     */
    static <T> ComponentType<T> of(
            Identifier identifier,
            Class<T> valueType,
            Supplier<T> defaultValue,
            Function<T, DataValue> encoder,
            Function<DataValue, T> decoder) {
        return ComponentType.<T>builder(identifier, valueType)
                .defaultValue(defaultValue)
                .encoder(encoder)
                .decoder(decoder)
                .build();
    }

    /**
     * Creates a version 1 component type whose encoded form is always a {@link DataMap}, which covers
     * nearly every component and spares the decoder a cast.
     *
     * @param <T>          The value type
     * @param identifier   Names the component
     * @param valueType    The class of value carried
     * @param defaultValue Supplies the value used when the component is absent
     * @param encoder      Converts a value into a map
     * @param decoder      Reconstructs a value from a map
     * @throws IllegalArgumentException If any argument is null
     * @return The component type
     */
    static <T> ComponentType<T> ofMap(
            Identifier identifier,
            Class<T> valueType,
            Supplier<T> defaultValue,
            Function<T, DataMap> encoder,
            Function<DataMap, T> decoder) {
        return ComponentType.<T>builder(identifier, valueType)
                .defaultValue(defaultValue)
                .mapCodec(encoder, decoder)
                .build();
    }

    /**
     * Starts building a component type. Prefer {@link #of} or {@link #ofMap} for the common
     * unversioned case; this exists for components that need a {@link #version()} and a migration.
     *
     * @param <T>        The value type
     * @param identifier Names the component
     * @param valueType  The class of value carried
     * @throws IllegalArgumentException If the identifier or value type is null
     * @return A builder
     */
    static <T> Builder<T> builder(Identifier identifier, Class<T> valueType) {
        return new Builder<>(identifier, valueType);
    }

    /**
     * Assembles a {@link ComponentType} from its parts.
     * <p>
     * A builder rather than another overload because a versioned component needs seven arguments,
     * and seven positional arguments of mostly-functions is a bug waiting to happen.
     *
     * @param <T> The value type
     */
    final class Builder<T> {
        private final Identifier identifier;
        private final Class<T> valueType;
        private Supplier<T> defaultValue;
        private Function<T, DataValue> encoder;
        private Function<DataValue, T> decoder;
        private int version = 1;
        private ComponentMigrator migrator;

        private Builder(Identifier identifier, Class<T> valueType) {
            if (identifier == null) {
                throw new IllegalArgumentException("Component identifier cannot be null");
            }
            if (valueType == null) {
                throw new IllegalArgumentException("Component value type cannot be null");
            }
            this.identifier = identifier;
            this.valueType = valueType;
        }

        /**
         * @param defaultValue Supplies the value used when the component is absent
         * @return This builder
         */
        public Builder<T> defaultValue(Supplier<T> defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        /**
         * @param encoder Converts a value into its persistable form
         * @return This builder
         */
        public Builder<T> encoder(Function<T, DataValue> encoder) {
            this.encoder = encoder;
            return this;
        }

        /**
         * @param decoder Reconstructs a value from its persistable form
         * @return This builder
         */
        public Builder<T> decoder(Function<DataValue, T> decoder) {
            this.decoder = decoder;
            return this;
        }

        /**
         * Sets both halves of the codec for the common case of a component stored as a
         * {@link DataMap}, adding the cast the decoder would otherwise have to write itself.
         *
         * @param encoder Converts a value into a map
         * @param decoder Reconstructs a value from a map
         * @throws IllegalArgumentException If either argument is null
         * @return This builder
         */
        public Builder<T> mapCodec(Function<T, DataMap> encoder, Function<DataMap, T> decoder) {
            if (encoder == null || decoder == null) {
                throw new IllegalArgumentException("Encoder and decoder cannot be null");
            }
            this.encoder = encoder::apply;
            this.decoder = data -> {
                if (!(data instanceof DataMap map)) {
                    throw new DataCodecException("Expected a DataMap for component " + identifier + ", but found "
                            + data.getClass().getSimpleName());
                }
                return decoder.apply(map);
            };
            return this;
        }

        /**
         * @param version The current stored shape, at least 1
         * @throws IllegalArgumentException If the version is below 1
         * @return This builder
         */
        public Builder<T> version(int version) {
            if (version < 1) {
                throw new IllegalArgumentException("Component version must be at least 1, got " + version);
            }
            this.version = version;
            return this;
        }

        /**
         * @param migrator Brings data written by older versions up to the current shape
         * @return This builder
         */
        public Builder<T> migrator(ComponentMigrator migrator) {
            this.migrator = migrator;
            return this;
        }

        /**
         * @throws IllegalArgumentException If the default value, encoder or decoder is missing, or
         *                                  if a version above 1 was set without a migrator — old data
         *                                  would then be unreadable with no way to recover it
         * @return The assembled component type
         */
        public ComponentType<T> build() {
            if (version > 1 && migrator == null) {
                throw new IllegalArgumentException("Component " + identifier + " declares version " + version
                        + " but supplies no migrator; data written by earlier versions would be unreadable");
            }
            return new FunctionalComponentType<>(
                    identifier, valueType, defaultValue, encoder, decoder, version, migrator);
        }
    }
}
