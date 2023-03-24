package dev.lukebemish.modularmetals.data.filter

import com.mojang.serialization.Codec
import groovy.transform.Immutable
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable

@Immutable
@CodecSerializable
class OrFilter extends Filter {
    List<Filter> values

    @Override
    Codec getCodec() {
        return $CODEC
    }

    @Override
    <T> boolean matches(T thing, FilterFinder<T> checker) {
        return values.any {it.matches(thing, checker)}
    }
}
