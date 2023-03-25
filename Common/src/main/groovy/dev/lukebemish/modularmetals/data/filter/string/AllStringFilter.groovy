package dev.lukebemish.modularmetals.data.filter.string

import com.mojang.serialization.Codec
import io.github.groovymc.cgl.api.transform.codec.ExposeCodec

@Singleton
class AllStringFilter extends StringFilter {
    @ExposeCodec
    static final Codec<AllStringFilter> ALL_CODEC = Codec.unit(AllStringFilter.instance)

    @Override
    Codec getCodec() {
        return ALL_CODEC
    }

    @Override
    <T> boolean matches(T thing, StringFilterFinder<T> checker) {
        return true
    }
}
