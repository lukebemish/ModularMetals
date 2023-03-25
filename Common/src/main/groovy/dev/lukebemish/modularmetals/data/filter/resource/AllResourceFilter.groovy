package dev.lukebemish.modularmetals.data.filter.resource

import com.mojang.serialization.Codec
import io.github.groovymc.cgl.api.transform.codec.ExposeCodec

@Singleton
class AllResourceFilter extends ResourceFilter {
    @ExposeCodec
    static final Codec<AllResourceFilter> ALL_CODEC = Codec.unit(AllResourceFilter.instance)

    @Override
    Codec getCodec() {
        return ALL_CODEC
    }

    @Override
    <T> boolean matches(T thing, ResourceFilterFinder<T> checker) {
        return true
    }
}
