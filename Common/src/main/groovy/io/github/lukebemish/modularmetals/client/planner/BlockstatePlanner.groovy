package io.github.lukebemish.modularmetals.client.planner

import com.google.gson.JsonElement
import com.mojang.serialization.JsonOps
import dev.lukebemish.dynamicassetgenerator.api.IPathAwareInputStreamSource
import dev.lukebemish.dynamicassetgenerator.api.ResourceGenerationContext
import io.github.groovymc.cgl.api.codec.ObjectOps
import io.github.lukebemish.modularmetals.Constants
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.IoSupplier

@Singleton
class BlockstatePlanner implements IPathAwareInputStreamSource {
    final Map<ResourceLocation, Map> sources = [:]

    @Override
    Set<ResourceLocation> getLocations() {
        return sources.keySet()
    }

    @Override
    IoSupplier<InputStream> get(ResourceLocation outRl, ResourceGenerationContext context) {
        Map obj = sources[outRl]
        if (obj === null)
            return null
        return {
            JsonElement json = ObjectOps.instance.convertTo(JsonOps.INSTANCE, obj)
            return new BufferedInputStream(new ByteArrayInputStream(Constants.GSON.toJson(json).bytes))
        }
    }

    void plan(ResourceLocation location, Map map) {
        sources[new ResourceLocation(location.namespace, "blockstates/${location.path}.json")] = map
    }
}
