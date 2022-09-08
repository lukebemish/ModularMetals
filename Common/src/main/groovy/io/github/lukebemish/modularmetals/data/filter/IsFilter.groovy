package io.github.lukebemish.modularmetals.data.filter

import com.mojang.serialization.Codec
import groovy.transform.CompileStatic
import groovy.transform.Immutable
import io.github.lukebemish.groovyduvet.wrapper.minecraft.api.codec.CodecSerializable
import net.minecraft.resources.ResourceLocation

@CompileStatic
@Immutable(knownImmutableClasses = [ResourceLocation])
@CodecSerializable
class IsFilter extends Filter {
    ResourceLocation value

    @Override
    Codec getCodec() {
        return $CODEC
    }

    @Override
    boolean matches(ResourceLocation rl) {
        return rl == value
    }
}
