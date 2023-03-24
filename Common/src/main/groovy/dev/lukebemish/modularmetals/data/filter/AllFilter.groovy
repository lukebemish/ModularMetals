package dev.lukebemish.modularmetals.data.filter

import com.mojang.serialization.Codec
import io.github.groovymc.cgl.api.transform.codec.ExposeCodec

@Singleton
class AllFilter extends Filter {
    @ExposeCodec
    static final Codec<AllFilter> ALL_CODEC = Codec.unit(AllFilter.instance)

    @Override
    Codec getCodec() {
        return ALL_CODEC
    }

    @Override
    <T> boolean matches(T thing, FilterFinder<T> checker) {
        return true
    }
}
