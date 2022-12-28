package io.github.lukebemish.modularmetals.data.filter

import com.mojang.serialization.Codec
import groovy.transform.CompileStatic
import groovy.transform.Immutable
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import net.minecraft.resources.ResourceLocation

@Immutable
@CodecSerializable
class NotFilter extends Filter {
    Filter value

    @Override
    Codec getCodec() {
        return $CODEC
    }

    @Override
    boolean matches(ResourceLocation rl) {
        return !value.matches(rl)
    }
}
