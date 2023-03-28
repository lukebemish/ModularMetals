package dev.lukebemish.modularmetals.data.filter.resource

import com.mojang.serialization.Codec
import io.github.groovymc.cgl.api.transform.codec.ExposeCodec

import java.util.function.Supplier

@Singleton
class AllResourceFilter extends ResourceFilter {
    @ExposeCodec
    static final Codec<AllResourceFilter> ALL_CODEC = Codec.unit({-> instance} as Supplier)

    @Override
    Codec getCodec() {
        return ALL_CODEC
    }

    @Override
    <T> boolean matches(T thing, ResourceFilterFinder<T> checker) {
        return true
    }
}
