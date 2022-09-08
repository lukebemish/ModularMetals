package io.github.lukebemish.modularmetals.client

import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import groovy.transform.CompileStatic
import io.github.lukebemish.modularmetals.Constants

import java.nio.charset.Charset
import java.util.function.Supplier

@CompileStatic
class LangBuilder {
    private static final Codec<Map<String,String>> CODEC = Codec.unboundedMap(Codec.STRING, Codec.STRING)
    private final Map<String, String> internal = new HashMap<>()

    void add(String key, String name) {
        internal.put(key,name)
    }

    Supplier<InputStream> build() {
        String json = Constants.GSON.toJson(CODEC.encodeStart(JsonOps.INSTANCE,internal).getOrThrow(false, {}))
        return { -> new ByteArrayInputStream(json.getBytes(Charset.forName('UTF-8'))) }
    }
}
