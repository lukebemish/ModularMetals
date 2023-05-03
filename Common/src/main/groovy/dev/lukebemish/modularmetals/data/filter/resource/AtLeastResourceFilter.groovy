package dev.lukebemish.modularmetals.data.filter.resource

import com.mojang.serialization.Codec
import groovy.transform.CompileStatic
import groovy.transform.Immutable
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable

@Immutable
@CodecSerializable
@CompileStatic
class AtLeastResourceFilter extends ResourceFilter {
    List<ResourceFilter> values
    int count

    @Override
    <T> boolean matches(T thing, ResourceFilterFinder<T> checker) {
        return values.findAll {it.matches(thing, checker)}.size() >= count
    }

    @Override
    Codec getCodec() {
        return $CODEC
    }
}
