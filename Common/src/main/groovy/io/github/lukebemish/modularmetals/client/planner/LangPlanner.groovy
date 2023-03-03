package io.github.lukebemish.modularmetals.client.planner

import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import io.github.lukebemish.modularmetals.Constants
import net.minecraft.server.packs.resources.IoSupplier

import java.nio.charset.Charset

@Singleton
class LangPlanner {
    private static final Codec<Map<String,String>> CODEC = Codec.unboundedMap(Codec.STRING, Codec.STRING)
    private final Map<String, Map<String, String>> internal = new HashMap<>()

    void add(String lang, String key, String name) {
        internal.computeIfAbsent(lang, k->new HashMap<>()).put(key,name)
    }

    IoSupplier<InputStream> build(String lang) {
        String json = Constants.GSON.toJson(CODEC.encodeStart(JsonOps.INSTANCE,internal.get(lang)).getOrThrow(false, {}))
        return { -> new ByteArrayInputStream(json.getBytes(Charset.forName('UTF-8'))) }
    }

    Set<String> languages() {
        return internal.keySet()
    }
}
