package io.github.lukebemish.modularmetals.data

import com.mojang.datafixers.util.Either
import groovy.transform.CompileStatic
import groovy.transform.Immutable
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import io.github.groovymc.cgl.api.transform.codec.WithCodec
import io.github.lukebemish.modularmetals.Constants
import io.github.lukebemish.modularmetals.ModularMetalsCommon
import io.github.lukebemish.modularmetals.data.filter.Filter
import net.minecraft.resources.ResourceLocation
import org.jetbrains.annotations.Nullable

@CompileStatic
@CodecSerializable
@Immutable(knownImmutableClasses = [Filter, Optional])
class Metal {
    final MetalTexturing texturing
    final Optional<Filter> disallowedVariants
    // Applied after disallowedVariants
    final Optional<Filter> allowedVariants
    final String name
    Map<ResourceLocation,ObjectHolder> properties = [:]
    final Optional<Filter> disallowedRecipes
    final Optional<Filter> allowedRecipes

    @Nullable ObjectHolder getPropertyFromMap(ResourceLocation rl) {
        return properties.get(rl)
    }

    @Immutable(knownImmutableClasses = [Optional, Either])
    @CodecSerializable
    static class MetalTexturing {
        final MapHolder generator
        @WithCodec(value = { ModConfig.TEMPLATE_SET_CODEC })
        Map<ResourceLocation,Map<String,Either<ResourceLocation,MapHolder>>> templateOverrides = [:]
        List<ResourceLocation> templateSets = []

        Map<String, Either<ResourceLocation,MapHolder>> getResolvedTemplateOverrides(ResourceLocation location) {
            Map<String, Either<ResourceLocation,MapHolder>> built = [:]
            for (ResourceLocation l : templateSets) {
                if (ModularMetalsCommon.config.templateSets.containsKey(l)) {
                    Map<ResourceLocation, Map<String, Either<ResourceLocation,MapHolder>>> templateSet = ModularMetalsCommon.config.templateSets.get(l)
                    if (templateSet.containsKey(location))
                        built.putAll(templateSet.get(location))
                } else {
                    Constants.LOGGER.warn("Missing referenced template set ${l}; ignoring.")
                }
            }
            built.putAll(templateOverrides.get(location)?:[:])
            return built
        }
    }
}
