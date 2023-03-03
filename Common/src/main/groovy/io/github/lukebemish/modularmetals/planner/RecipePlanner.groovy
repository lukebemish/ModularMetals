package io.github.lukebemish.modularmetals.planner

import com.google.gson.JsonElement
import dev.lukebemish.dynamicassetgenerator.api.IPathAwareInputStreamSource
import dev.lukebemish.dynamicassetgenerator.api.ResourceGenerationContext
import io.github.lukebemish.modularmetals.Constants
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.IoSupplier

@Singleton
class RecipePlanner implements IPathAwareInputStreamSource {
    final Map<ResourceLocation, JsonElement> sources = [:]

    void plan(ResourceLocation location, JsonElement json) {
        sources[new ResourceLocation(location.namespace, "recipes/${location.path}.json")] = json
    }

    @Override
    Set<ResourceLocation> getLocations() {
        return sources.keySet()
    }

    @Override
    IoSupplier<InputStream> get(ResourceLocation outRl, ResourceGenerationContext context) {
        JsonElement json = sources.get(outRl)
        if (json===null)
            return null
        return {->
            return new BufferedInputStream(new ByteArrayInputStream(Constants.GSON.toJson(json).bytes))
        }
    }
}
