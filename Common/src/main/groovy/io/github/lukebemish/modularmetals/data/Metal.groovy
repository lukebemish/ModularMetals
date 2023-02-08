package io.github.lukebemish.modularmetals.data


import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import io.github.groovymc.cgl.api.transform.codec.WithCodec
import io.github.lukebemish.modularmetals.Constants
import io.github.lukebemish.modularmetals.ModularMetalsCommon
import net.minecraft.resources.ResourceLocation
import org.jetbrains.annotations.Nullable

@CodecSerializable(allowDefaultValues = true)
@TupleConstructor
class Metal {
    final MetalTexturing texturing
    final String name
    final List<ResourceLocation> categories
    final Map<ResourceLocation,ObjectHolder> properties = [:]

    @Nullable ObjectHolder getPropertyFromMap(ResourceLocation rl) {
        return properties.get(rl)
    }

    @TupleConstructor
    @CodecSerializable(allowDefaultValues = true)
    static class MetalTexturing {
        final MapHolder generator
        @WithCodec(value = { ModConfig.TEMPLATE_SET_CODEC })
        final Map<ResourceLocation,TexSourceMap> templateOverrides = [:]
        final List<ResourceLocation> templateSets = []

        Map<String, MapHolder> getResolvedTemplateOverrides(ResourceLocation location) {
            Map<String, MapHolder> built = [:]
            for (ResourceLocation l : templateSets) {
                if (ModularMetalsCommon.config.templateSets.containsKey(l)) {
                    Map<ResourceLocation, TexSourceMap> templateSet = ModularMetalsCommon.config.templateSets.get(l)
                    if (templateSet.containsKey(location))
                        built.putAll(templateSet.get(location).value)
                } else {
                    Constants.LOGGER.warn("Missing referenced template set ${l}; ignoring.")
                }
            }
            built.putAll(templateOverrides.get(location)?.value?:[:])
            return built
        }
    }
}
