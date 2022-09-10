package io.github.lukebemish.modularmetals.data

import com.mojang.datafixers.util.Either
import groovy.transform.CompileStatic
import groovy.transform.Immutable
import io.github.lukebemish.groovyduvet.wrapper.minecraft.api.codec.CodecSerializable
import io.github.lukebemish.modularmetals.Constants
import io.github.lukebemish.modularmetals.ModularMetalsCommon
import io.github.lukebemish.modularmetals.data.filter.Filter
import net.minecraft.resources.ResourceLocation
import org.jetbrains.annotations.Nullable

@CompileStatic
@CodecSerializable(camelToSnake = true, allowDefaultValues = true)
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
    @CodecSerializable(camelToSnake = true, allowDefaultValues = true)
    static class MetalTexturing {
        final MapHolder generator
        Either<Map<ResourceLocation,Either<ResourceLocation, Map<String,ResourceLocation>>>,List<ResourceLocation>> templateOverrides = Either.left([:])

        Map<String, ResourceLocation> getResolvedTemplateOverrides(ResourceLocation location) {
            return templateOverrides.map({map ->
                return map.get(location)?.map({
                    return ['':it]
                },{
                    return it
                })?:[:]
            },{
                Map<String, ResourceLocation> built = [:]
                for (ResourceLocation l : it) {
                    if (ModularMetalsCommon.config.templateSets.containsKey(l)) {
                        Map<ResourceLocation, Map<String, ResourceLocation>> templateSet = ModularMetalsCommon.config.templateSets.get(l)
                        if (templateSet.containsKey(location))
                            built.putAll(templateSet.get(location))
                    } else {
                        Constants.LOGGER.warn("Missing referenced template set ${l}; ignoring.")
                    }
                }
                return built
            })
        }
    }
}
