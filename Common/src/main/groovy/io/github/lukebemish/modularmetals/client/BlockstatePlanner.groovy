package io.github.lukebemish.modularmetals.client

import com.google.gson.JsonElement
import com.mojang.serialization.JsonOps
import groovy.transform.CompileStatic
import io.github.lukebemish.dynamic_asset_generator.api.IPathAwareInputStreamSource
import io.github.lukebemish.groovyduvet.wrapper.minecraft.api.codec.ObjectOps
import io.github.lukebemish.modularmetals.Constants
import net.minecraft.resources.ResourceLocation

import java.util.function.Supplier

@CompileStatic
@Singleton
class BlockstatePlanner implements IPathAwareInputStreamSource {
    final Map<ResourceLocation, Map> sources = [:]

    @Override
    Set<ResourceLocation> getLocations() {
        return sources.keySet()
    }

    @Override
    Supplier<InputStream> get(ResourceLocation outRl) {
        return {
            Map obj = sources[outRl]
            if (obj === null)
                return null
            JsonElement json = ObjectOps.instance.convertTo(JsonOps.INSTANCE, obj)
            return new BufferedInputStream(new ByteArrayInputStream(Constants.GSON.toJson(json).bytes))
        }
    }



    void plan(ResourceLocation location, Map map) {
        sources[new ResourceLocation(location.namespace, "blockstates/${location.path}.json")] = map
    }
}