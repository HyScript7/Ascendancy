package io.github.hyscript7.ascendancy.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.hyscript7.ascendancy.api.data.DataBoolean;
import io.github.hyscript7.ascendancy.api.data.DataBytes;
import io.github.hyscript7.ascendancy.api.data.DataDecimal;
import io.github.hyscript7.ascendancy.api.data.DataInteger;
import io.github.hyscript7.ascendancy.api.data.DataList;
import io.github.hyscript7.ascendancy.api.data.DataMap;
import io.github.hyscript7.ascendancy.api.data.DataStorageException;
import io.github.hyscript7.ascendancy.api.data.DataString;
import io.github.hyscript7.ascendancy.api.data.DataValue;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Converts {@link DataValue} to and from Gson's tree model.
 * <p>
 * Confined to this class on purpose: {@code JsonElement} never appears in {@code :api}, so swapping
 * the on-disk format later is a {@code :core} change that no content pack can notice.
 * <p>
 * Two places where the value tree does not map cleanly onto JSON, both handled here:
 * <ul>
 *   <li><strong>Integer versus decimal.</strong> JSON has one number type. On read, a literal
 *       containing {@code .}, {@code e} or {@code E} becomes a {@link DataDecimal}; anything else
 *       becomes a {@link DataInteger}.</li>
 *   <li><strong>Byte arrays.</strong> JSON has none, so a {@link DataBytes} is written as
 *       <code>{"$b64": "..."}</code>. {@link DataMap} rejects keys beginning with {@code $}, which
 *       is what keeps that sentinel unambiguous.</li>
 * </ul>
 * Neither is elegant. Both are the price of a format a server owner can open in a text editor.
 */
public final class JsonDataValueCodec {
    /** Marks an object as an encoded byte array rather than a map. */
    static final String BYTES_SENTINEL = "$b64";

    private JsonDataValueCodec() {}

    /**
     * Encodes a value into Gson's tree model.
     *
     * @param value The value to encode
     * @return The equivalent JSON element
     */
    public static JsonElement encode(DataValue value) {
        return switch (value) {
            case DataString string -> new JsonPrimitive(string.value());
            case DataInteger integer -> new JsonPrimitive(integer.value());
            case DataDecimal decimal -> new JsonPrimitive(decimal.value());
            case DataBoolean bool -> new JsonPrimitive(bool.value());
            case DataBytes bytes -> {
                JsonObject wrapper = new JsonObject();
                wrapper.addProperty(BYTES_SENTINEL, Base64.getEncoder().encodeToString(bytes.value()));
                yield wrapper;
            }
            case DataList list -> {
                JsonArray array = new JsonArray();
                list.values().forEach(element -> array.add(encode(element)));
                yield array;
            }
            case DataMap map -> {
                JsonObject object = new JsonObject();
                // Sorted so that a saved file produces a stable diff instead of churning on every write.
                new TreeMap<>(map.entries()).forEach((key, entry) -> object.add(key, encode(entry)));
                yield object;
            }
        };
    }

    /**
     * Decodes a value from Gson's tree model.
     *
     * @param element The JSON element to decode
     * @throws DataStorageException If the element is JSON null, or a primitive of an unrecognised
     *                              kind — both of which mean the file has been hand-edited into a
     *                              shape we never write
     * @return The equivalent value
     */
    public static DataValue decode(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            // DataValue has no null member, deliberately: absent and null-valued are the same thing.
            throw new DataStorageException("JSON null is not a representable DataValue");
        }
        if (element.isJsonObject()) {
            return decodeObject(element.getAsJsonObject());
        }
        if (element.isJsonArray()) {
            List<DataValue> values = new ArrayList<>();
            element.getAsJsonArray().forEach(child -> values.add(decode(child)));
            return new DataList(values);
        }
        return decodePrimitive(element.getAsJsonPrimitive());
    }

    private static DataValue decodeObject(JsonObject object) {
        if (object.size() == 1 && object.has(BYTES_SENTINEL)) {
            String encoded = object.get(BYTES_SENTINEL).getAsString();
            try {
                return new DataBytes(Base64.getDecoder().decode(encoded));
            } catch (IllegalArgumentException exception) {
                throw new DataStorageException("Malformed base64 in a " + BYTES_SENTINEL + " value", exception);
            }
        }
        Map<String, DataValue> entries = new java.util.LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            // Reserved keys are backend bookkeeping and never part of the value the caller stored.
            if (entry.getKey().startsWith("$")) {
                continue;
            }
            entries.put(entry.getKey(), decode(entry.getValue()));
        }
        return new DataMap(entries);
    }

    private static DataValue decodePrimitive(JsonPrimitive primitive) {
        if (primitive.isBoolean()) {
            return DataBoolean.of(primitive.getAsBoolean());
        }
        if (primitive.isString()) {
            return new DataString(primitive.getAsString());
        }
        if (primitive.isNumber()) {
            // Gson hands back a LazilyParsedNumber, so the literal text is the only thing that still
            // knows whether the author wrote 1 or 1.0.
            String literal = primitive.getAsString();
            if (literal.contains(".") || literal.contains("e") || literal.contains("E")) {
                return new DataDecimal(primitive.getAsDouble());
            }
            try {
                return new DataInteger(Long.parseLong(literal));
            } catch (NumberFormatException exception) {
                // A whole number too large for a long. Better to keep it approximately than to fail.
                return new DataDecimal(primitive.getAsDouble());
            }
        }
        throw new DataStorageException("Unrecognised JSON primitive: " + primitive);
    }
}
