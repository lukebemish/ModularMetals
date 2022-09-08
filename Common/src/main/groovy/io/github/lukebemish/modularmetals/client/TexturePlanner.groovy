package io.github.lukebemish.modularmetals.client

import com.google.gson.JsonSyntaxException
import com.mojang.blaze3d.platform.NativeImage
import groovy.transform.CompileStatic
import io.github.lukebemish.dynamic_asset_generator.api.IPathAwareInputStreamSource
import io.github.lukebemish.modularmetals.Constants
import net.minecraft.resources.ResourceLocation

import java.util.function.Supplier

@CompileStatic
@Singleton
class TexturePlanner implements IPathAwareInputStreamSource {
    final Map<ResourceLocation, Supplier<NativeImage>> sources = [:]

    @Override
    Set<ResourceLocation> getLocations() {
        return sources.keySet()
    }

    @Override
    Supplier<InputStream> get(ResourceLocation outRl) {
        return {
            try {
                return sources.get(outRl)?.get()?.with {
                    new ByteArrayInputStream(it.asByteArray())
                }
            } catch (IOException e) {
                Constants.LOGGER.error("Could not write image to stream: {}", outRl, e)
            } catch (JsonSyntaxException e) {
                Constants.LOGGER.error("Issue loading texture source JSON for output: {}", outRl, e)
            } catch (Exception remainder) {
                Constants.LOGGER.error("Issue creating texture from source JSON for output: {}",outRl, remainder)
            }
            return null
        }
    }

    void plan(ResourceLocation location, Supplier<NativeImage> supplier) {
        sources[new ResourceLocation(location.namespace, "textures/${location.path}.png")] = supplier
    }
}
