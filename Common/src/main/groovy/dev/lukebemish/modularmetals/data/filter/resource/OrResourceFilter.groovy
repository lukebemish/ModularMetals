package dev.lukebemish.modularmetals.data.filter.resource

import com.mojang.serialization.Codec
import groovy.transform.Immutable
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable

@Immutable
@CodecSerializable
class OrResourceFilter extends ResourceFilter {
    List<ResourceFilter> values

    @Override
    Codec getCodec() {
        return $CODEC
    }

    @Override
    <T> boolean matches(T thing, ResourceFilterFinder<T> checker) {
        return values.any {it.matches(thing, checker)}
    }
}
