package io.github.lukebemish.modularmetals.data.filter

import com.mojang.serialization.Codec
import groovy.transform.Immutable
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import net.minecraft.resources.ResourceLocation

@Immutable(knownImmutableClasses = [ResourceLocation])
@CodecSerializable
class TagFilter extends Filter {
    ResourceLocation value

    @Override
    Codec getCodec() {
        return $CODEC
    }

    @Override
    <T> boolean matches(T thing, FilterFinder<T> checker) {
        return checker.isTag(thing, value)
    }
}
