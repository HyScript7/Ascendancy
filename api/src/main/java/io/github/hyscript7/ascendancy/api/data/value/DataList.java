package io.github.hyscript7.ascendancy.api.data.value;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A {@link DataValue} holding an ordered sequence of values.
 * <p>
 * The list is heterogeneous by type — nothing stops a caller mixing strings and maps — because the
 * component's own codec is the thing that knows what shape it wrote.
 *
 * @param values The wrapped values, unmodifiable and never null
 */
public record DataList(List<DataValue> values) implements DataValue {
    /**
     * @throws IllegalArgumentException If the list or any element is null
     */
    public DataList {
        if (values == null) {
            throw new IllegalArgumentException("DataList values cannot be null");
        }
        // List.copyOf rejects nulls for us, but with a NullPointerException that says nothing useful.
        for (DataValue value : values) {
            if (value == null) {
                throw new IllegalArgumentException("DataList cannot contain null elements");
            }
        }
        values = List.copyOf(values);
    }

    /**
     * @return An empty list
     */
    public static DataList empty() {
        return new DataList(List.of());
    }

    /**
     * @param values The values to wrap
     * @throws IllegalArgumentException If any element is null
     * @return A list holding the given values
     */
    public static DataList of(DataValue... values) {
        return new DataList(List.of(values));
    }

    /**
     * Maps a collection of arbitrary objects into a list, which is the common case when encoding a
     * component that holds a collection.
     *
     * @param <T>      The source element type
     * @param source   The elements to encode
     * @param encoder  Converts one element into a {@link DataValue}
     * @throws IllegalArgumentException If the source or encoder is null, or the encoder returns null
     * @return A list holding the encoded elements
     */
    public static <T> DataList from(Iterable<T> source, Function<? super T, ? extends DataValue> encoder) {
        if (source == null || encoder == null) {
            throw new IllegalArgumentException("Source and encoder cannot be null");
        }
        List<DataValue> encoded = new ArrayList<>();
        for (T element : source) {
            encoded.add(encoder.apply(element));
        }
        return new DataList(encoded);
    }

    /**
     * @return The number of elements
     */
    public int size() {
        return values.size();
    }

    /**
     * @return True if there are no elements
     */
    public boolean isEmpty() {
        return values.isEmpty();
    }

    /**
     * @param index The index to read
     * @throws IndexOutOfBoundsException If the index is out of range
     * @return The value at that index
     */
    public DataValue get(int index) {
        return values.get(index);
    }

    /**
     * @return A stream over the elements
     */
    public Stream<DataValue> stream() {
        return values.stream();
    }
}
