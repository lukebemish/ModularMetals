package io.github.lukebemish.modularmetals

import com.google.gson.JsonElement
import groovy.transform.CompileStatic
import io.github.lukebemish.dynamic_asset_generator.api.IPathAwareInputStreamSource
import net.minecraft.resources.ResourceLocation

import java.util.function.Supplier

@CompileStatic
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
    Supplier<InputStream> get(ResourceLocation outRl) {
        return {->
            JsonElement json = sources.get(outRl)
            if (json===null)
                return null
            return new BufferedInputStream(new ByteArrayInputStream(Constants.GSON.toJson(json).bytes))
        }
    }
}
