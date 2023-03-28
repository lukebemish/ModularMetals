package dev.lukebemish.modularmetals.data.filter.string

import com.mojang.serialization.Codec
import io.github.groovymc.cgl.api.transform.codec.ExposeCodec

import java.util.function.Supplier

@Singleton
class NoneStringFilter extends StringFilter {
    @ExposeCodec
    static final Codec<NoneStringFilter> NONE_CODEC = Codec.unit({-> instance} as Supplier)

    @Override
    <T> boolean matches(T thing, StringFilterFinder<T> checker) {
        return false
    }

    @Override
    Codec getCodec() {
        return NONE_CODEC
    }
}
