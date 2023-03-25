package dev.lukebemish.modularmetals.data.filter.resource

import com.mojang.serialization.Codec
import groovy.transform.Immutable
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import net.minecraft.resources.ResourceLocation

@Immutable(knownImmutableClasses = [ResourceLocation])
@CodecSerializable
class IsResourceFilter extends ResourceFilter {
    ResourceLocation value

    @Override
    Codec getCodec() {
        return $CODEC
    }

    @Override
    <T> boolean matches(T thing, ResourceFilterFinder<T> checker) {
        return checker.isLocation(thing, value)
    }
}
