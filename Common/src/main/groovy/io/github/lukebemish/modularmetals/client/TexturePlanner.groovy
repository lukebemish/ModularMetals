package io.github.lukebemish.modularmetals.client

import com.google.common.base.Suppliers
import com.google.gson.JsonSyntaxException
import com.mojang.blaze3d.platform.NativeImage
import groovy.transform.CompileStatic
import io.github.lukebemish.dynamic_asset_generator.api.IPathAwareInputStreamSource
import io.github.lukebemish.dynamic_asset_generator.api.client.generators.TextureMetaGenerator
import io.github.lukebemish.modularmetals.Constants
import net.minecraft.resources.ResourceLocation

import java.util.function.Supplier

@CompileStatic
@Singleton
class TexturePlanner implements IPathAwareInputStreamSource {
    final Map<ResourceLocation, Supplier<NativeImage>> sources = [:]
    final Map<ResourceLocation, Supplier<TextureMetaGenerator>> sourceMetas = [:]

    @Override
    Set<ResourceLocation> getLocations() {
        return sources.keySet() + sourceMetas.keySet()
    }

    @Override
    Supplier<InputStream> get(ResourceLocation outRl) {
        return {
            try {
                return sources.get(outRl)?.get()?.with {
                    new ByteArrayInputStream(it.asByteArray())
                }?:sourceMetas.get(outRl)?.get()?.with {
                    it.get(outRl)?.get()
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

    void plan(ResourceLocation location, Supplier<NativeImage> supplier, List<String> sourceTextures) {
        sources[new ResourceLocation(location.namespace, "textures/${location.path}.png")] = supplier
        if (sourceTextures.size() > 0)
            sourceMetas[new ResourceLocation(location.namespace, "textures/${location.path}.png.mcmeta")] = Suppliers.memoize({->
                new TextureMetaGenerator(sourceTextures.collect {ResourceLocation.of(it,':' as char)}, Optional.empty(), Optional.empty(), Optional.empty(), location)
            })
    }
}
