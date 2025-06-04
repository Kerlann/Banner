package com.mohistmc.banner.boot;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// code from gson, Apache license
// mute the assertion error throwing because we dynamically add elements to enums
public class EnumTypeFactory implements TypeAdapterFactory {

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<? super T> rawType = type.getRawType();
        if (!Enum.class.isAssignableFrom(rawType) || rawType == Enum.class) {
            return null;
        }
        if (!rawType.isEnum()) {
            rawType = rawType.getSuperclass(); // handle anonymous subclasses
        }
        return (TypeAdapter<T>) new EnumTypeAdapter(rawType);
    }

    private static final class EnumTypeAdapter<T extends Enum<T>> extends TypeAdapter<T> {

        private final Map<String, T> nameToConstant = new HashMap<>();
        private final Map<T, String> constantToName = new HashMap<>();
        public EnumTypeAdapter(Class<T> classOfT) {
            for (T constant : classOfT.getEnumConstants()) {
                if (constant == null) {          // ① ignorer les “trous”
                    continue;
                }

                String name = constant.name();
                SerializedName ann;
                try {
                    ann = classOfT.getField(name).getAnnotation(SerializedName.class);
                } catch (NoSuchFieldException ignored) {
                    ann = null;
                }

                if (ann != null) {
                    name = ann.value();
                    for (String alt : ann.alternate()) {
                        nameToConstant.put(alt, constant);
                    }
                }
                nameToConstant.put(name, constant);
                constantToName.put(constant, name);
            }
        }

        @Override
        public T read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            String key = in.nextString();
            T constant = nameToConstant.get(key);
            if (constant == null) {              // ② valeur inconnue : on lève ou on ignore
                 return null;                  // ← laisser Essentials gérer
                //throw new IllegalArgumentException("Unknown enum constant: " + key);
            }
            return constant;
        }

        @Override
        public void write(JsonWriter out, T value) throws IOException {
            out.value(value == null ? null : constantToName.get(value));
        }
    }
}
