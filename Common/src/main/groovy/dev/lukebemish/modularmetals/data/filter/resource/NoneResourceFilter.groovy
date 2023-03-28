package dev.lukebemish.modularmetals.data.filter.resource

import com.mojang.serialization.Codec
import io.github.groovymc.cgl.api.transform.codec.ExposeCodec

import java.util.function.Supplier

@Singleton
class NoneResourceFilter extends ResourceFilter {
    @ExposeCodec
    static final Codec<NoneResourceFilter> NONE_CODEC = Codec.unit({-> instance} as Supplier)

    @Override
    <T> boolean matches(T thing, ResourceFilterFinder<T> checker) {
        return false
    }

    @Override
    Codec getCodec() {
        return NONE_CODEC
    }
}
