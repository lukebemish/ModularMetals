package io.github.lukebemish.modularmetals.data.filter

import com.mojang.serialization.Codec
import groovy.transform.CompileStatic
import groovy.transform.Immutable
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import net.minecraft.resources.ResourceLocation

@CompileStatic
@Immutable
@CodecSerializable
class AndFilter extends Filter {
    List<Filter> values

    @Override
    Codec getCodec() {
        return $CODEC
    }

    @Override
    boolean matches(ResourceLocation rl) {
        return values.every {it.matches rl}
    }
}
