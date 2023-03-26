package dev.lukebemish.modularmetals.client.planner

import com.google.common.base.Suppliers
import com.google.gson.JsonSyntaxException
import com.mojang.blaze3d.platform.NativeImage
import dev.lukebemish.dynamicassetgenerator.api.IPathAwareInputStreamSource
import dev.lukebemish.dynamicassetgenerator.api.ResourceGenerationContext
import dev.lukebemish.dynamicassetgenerator.api.client.generators.ITexSource
import dev.lukebemish.dynamicassetgenerator.api.client.generators.TexSourceDataHolder
import dev.lukebemish.dynamicassetgenerator.api.client.generators.TextureMetaGenerator
import dev.lukebemish.dynamicassetgenerator.api.client.generators.texsources.Overlay
import dev.lukebemish.modularmetals.Constants
import dev.lukebemish.modularmetals.data.texsources.CachelessSource
import groovy.transform.CompileStatic
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.IoSupplier
import org.jetbrains.annotations.Nullable

import java.util.function.Function
import java.util.function.Supplier

@Singleton
@CompileStatic
class TexturePlanner implements IPathAwareInputStreamSource {
    final Map<ResourceLocation, Function<ResourceGenerationContext, NativeImage>> sources = [:]
    final Map<ResourceLocation, Supplier<TextureMetaGenerator>> sourceMetas = [:]
    final Map<ResourceLocation, List<CachelessSource>> armorSources = [:]

    @Override
    Set<ResourceLocation> getLocations() {
        return sources.keySet() + sourceMetas.keySet() + armorSources.keySet()
    }

    @Override
    IoSupplier<InputStream> get(ResourceLocation outRl, ResourceGenerationContext context) {
        return {
            try {
                return sources.get(outRl)?.apply(context)?.with {
                    new ByteArrayInputStream(it.asByteArray())
                }?:sourceMetas.get(outRl)?.get()?.with {
                    it.get(outRl, context)?.get()
                }?:armorSources.get(outRl)?.with {
                    Overlay source = new Overlay(it.collect {(ITexSource)it})
                    return new ByteArrayInputStream(source.getSupplier(new TexSourceDataHolder(), context).get().asByteArray())
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

    void planArmor(ResourceLocation material, @Nullable Function<ResourceGenerationContext, NativeImage> layer, boolean is1) {
        var location = new ResourceLocation(material.namespace, "textures/models/armor/${material.path}_layer_${is1 ? '1' : '2'}.png")
        armorSources.computeIfAbsent(location, {k->[]}).add(new CachelessSource(layer))
    }

    void plan(ResourceLocation location, Function<ResourceGenerationContext, NativeImage> imageSource, List<String> sourceTextures) {
        sources[new ResourceLocation(location.namespace, "textures/${location.path}.png")] = imageSource
        if (sourceTextures.size() > 0)
            sourceMetas[new ResourceLocation(location.namespace, "textures/${location.path}.png.mcmeta")] = Suppliers.memoize({->
                new TextureMetaGenerator(sourceTextures.collect {ResourceLocation.of(it,':' as char)}, Optional.empty(), Optional.empty(), Optional.empty(), location)
            })
    }
}
