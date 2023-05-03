package dev.lukebemish.modularmetals.data.filter.string

import com.mojang.serialization.Codec
import groovy.transform.CompileStatic
import groovy.transform.Immutable
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable

@Immutable
@CompileStatic
@CodecSerializable
class AtLeastStringFilter extends StringFilter {
    List<StringFilter> values
    int count

    @Override
    <T> boolean matches(T thing, StringFilterFinder<T> checker) {
        return values.findAll {it.matches(thing, checker)}.size() >= count
    }

    @Override
    Codec getCodec() {
        return $CODEC
    }
}
