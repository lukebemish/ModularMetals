package dev.lukebemish.modularmetals.data.filter.string

import com.mojang.serialization.Codec
import groovy.transform.Immutable
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable

@Immutable
@CodecSerializable
class AndStringFilter extends StringFilter {
    List<StringFilter> values

    @Override
    Codec getCodec() {
        return $CODEC
    }

    @Override
    <T> boolean matches(T thing, StringFilterFinder<T> checker) {
        return values.every {it.matches(thing, checker)}
    }
}
