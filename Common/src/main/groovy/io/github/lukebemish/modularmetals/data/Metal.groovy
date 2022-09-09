package io.github.lukebemish.modularmetals.data

import com.mojang.datafixers.util.Either
import groovy.transform.CompileStatic
import groovy.transform.Immutable
import io.github.lukebemish.groovyduvet.wrapper.minecraft.api.codec.CodecSerializable
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

    @Nullable ObjectHolder getPropertyFromMap(ResourceLocation rl) {
        return properties.get(rl)
    }

    @Immutable(knownImmutableClasses = [Optional])
    @CodecSerializable(camelToSnake = true, allowDefaultValues = true)
    static class MetalTexturing {
        final MapHolder generator
        Map<ResourceLocation,Either<ResourceLocation, Map<String,ResourceLocation>>> templateOverrides = [:]
    }
}
