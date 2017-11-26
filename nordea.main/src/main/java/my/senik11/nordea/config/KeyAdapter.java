package my.senik11.nordea.config;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.googlecode.objectify.Key;

import java.lang.reflect.Type;

/**
 * @author Arseny Krasenkov {@literal <akrasenkov@at-consulting.ru>}
 */
public class KeyAdapter implements JsonSerializer<Key>, JsonDeserializer<Key> {
    @Override
    public JsonElement serialize(Key src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.getString());
    }

    @Override
    public Key deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json == null) {
            return null;
        }
        return Key.create(json.getAsString());
    }
}
