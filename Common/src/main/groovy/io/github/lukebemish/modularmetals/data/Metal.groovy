package io.github.lukebemish.modularmetals.data

import groovy.transform.CompileStatic
import groovy.transform.Immutable
import io.github.lukebemish.groovyduvet.wrapper.minecraft.api.codec.CodecSerializable
import io.github.lukebemish.modularmetals.data.filter.Filter
import net.minecraft.resources.ResourceLocation
import org.jetbrains.annotations.Nullable

@CompileStatic
@CodecSerializable
@Immutable(knownImmutableClasses = [Filter, Optional])
class Metal {
    final MapHolder texturing
    final Optional<Filter> disallowedVariants
    // Applied after disallowedVariants
    final Optional<Filter> allowedVariants
    final String name
    final Optional<Map<ResourceLocation,ObjectHolder>> properties

    @Nullable ObjectHolder getPropertyFromMap(ResourceLocation rl) {
        return properties.map {Optional.ofNullable(it.get(rl))}.orElse(Optional.empty()).orElse(null)
    }

    Optional<ObjectHolder> getOptionalPropertyFromMap(ResourceLocation rl) {
        return Optional.ofNullable(getPropertyFromMap(rl))
    }
}
