package dev.lukebemish.modularmetals.data.filter.resource

import com.mojang.serialization.Codec
import groovy.transform.Immutable
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable

@Immutable
@CodecSerializable
class NotResourceFilter extends ResourceFilter {
    ResourceFilter value

    @Override
    Codec getCodec() {
        return $CODEC
    }

    @Override
    <T> boolean matches(T thing, ResourceFilterFinder<T> checker) {
        return !value.matches(thing, checker)
    }
}
