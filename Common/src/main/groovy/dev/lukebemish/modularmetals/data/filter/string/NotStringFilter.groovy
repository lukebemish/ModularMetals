package dev.lukebemish.modularmetals.data.filter.string

import com.mojang.serialization.Codec
import groovy.transform.Immutable
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable

@Immutable
@CodecSerializable
class NotStringFilter extends StringFilter {
    StringFilter value

    @Override
    Codec getCodec() {
        return $CODEC
    }

    @Override
    <T> boolean matches(T thing, StringFilterFinder<T> checker) {
        return !value.matches(thing, checker)
    }
}
