package dev.lukebemish.modularmetals.data.filter.string

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import dev.lukebemish.modularmetals.PsuedoRegisters
import dev.lukebemish.modularmetals.util.CodecAware
import dev.lukebemish.modularmetals.util.CodecMapCodec
import groovy.transform.CompileStatic
import groovy.transform.KnownImmutable
import io.github.groovymc.cgl.api.transform.codec.ExposeCodec

@KnownImmutable
@CompileStatic
abstract class StringFilter implements CodecAware {
    @ExposeCodec
    static final Codec<StringFilter> CODEC = Codec.either(CodecMapCodec.dispatch(PsuedoRegisters.RESOURCE_FILTER_TYPES, "filter", true),
        Codec.STRING.<IsStringFilter>xmap({new IsStringFilter(it)}, {it.value})).<StringFilter>xmap({
        it.map({it},{it})
    },{ it instanceof IsStringFilter ? Either.right(it) : Either.left(it)})

    abstract <T> boolean matches(T thing, StringFilterFinder<T> checker)

    public static final StringFilterFinder<String> SIMPLE_FINDER = new StringFilterFinder<String>() {
        @Override
        boolean isLocation(String thing, String location) {
            return thing == location
        }
    }

    public static final StringFilterFinder<Set<String>> SET_FINDER = new StringFilterFinder<Set<String>>() {
        @Override
        boolean isLocation(Set<String> thing, String location) {
            return thing.contains(location)
        }
    }
}
