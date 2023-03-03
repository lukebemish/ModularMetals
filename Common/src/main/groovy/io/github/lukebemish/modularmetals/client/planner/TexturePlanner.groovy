package io.github.lukebemish.modularmetals.client.planner

import com.google.common.base.Suppliers
import com.google.gson.JsonSyntaxException
import com.mojang.blaze3d.platform.NativeImage
import dev.lukebemish.dynamicassetgenerator.api.IPathAwareInputStreamSource
import dev.lukebemish.dynamicassetgenerator.api.ResourceGenerationContext
import dev.lukebemish.dynamicassetgenerator.api.client.generators.TextureMetaGenerator
import groovy.transform.CompileStatic
import io.github.lukebemish.modularmetals.Constants
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.IoSupplier

import java.util.function.Function
import java.util.function.Supplier

@Singleton
class TexturePlanner implements IPathAwareInputStreamSource {
    final Map<ResourceLocation, Function<ResourceGenerationContext, NativeImage>> sources = [:]
    final Map<ResourceLocation, Supplier<TextureMetaGenerator>> sourceMetas = [:]

    @Override
    Set<ResourceLocation> getLocations() {
        return sources.keySet() + sourceMetas.keySet()
    }

    @Override
    IoSupplier<InputStream> get(ResourceLocation outRl, ResourceGenerationContext context) {
        return {
            try {
                return sources.get(outRl)?.apply(context)?.with {
                    new ByteArrayInputStream(it.asByteArray())
                }?:sourceMetas.get(outRl)?.get()?.with {
                    it.get(outRl, context)?.get()
                }
            } catch (IOException e) {
                Constants.LOGGER.error("Could not write image to stream: {}", outRl, e)
                throw new IOException(e)
            } catch (JsonSyntaxException e) {
                Constants.LOGGER.error("Issue loading texture source JSON for output: {}", outRl, e)
                throw new IOException(e)
            } catch (Exception remainder) {
                Constants.LOGGER.error("Issue creating texture from source JSON for output: {}",outRl, remainder)
                throw new IOException(remainder)
            }
        }
    }

    void plan(ResourceLocation location, Function<ResourceGenerationContext, NativeImage> imageSource, List<String> sourceTextures) {
        sources[new ResourceLocation(location.namespace, "textures/${location.path}.png")] = imageSource
        if (sourceTextures.size() > 0)
            sourceMetas[new ResourceLocation(location.namespace, "textures/${location.path}.png.mcmeta")] = Suppliers.memoize({->
                new TextureMetaGenerator(sourceTextures.collect {ResourceLocation.of(it,':' as char)}, Optional.empty(), Optional.empty(), Optional.empty(), location)
            })
    }
}
