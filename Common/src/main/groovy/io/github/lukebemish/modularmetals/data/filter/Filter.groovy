package io.github.lukebemish.modularmetals.data.filter

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import groovy.transform.KnownImmutable
import io.github.groovymc.cgl.api.transform.codec.ExposeCodec
import io.github.lukebemish.modularmetals.PsuedoRegisters
import io.github.lukebemish.modularmetals.util.CodecAware
import io.github.lukebemish.modularmetals.util.CodecMapCodec
import net.minecraft.resources.ResourceLocation

@KnownImmutable
abstract class Filter implements CodecAware {
    @ExposeCodec
    static final Codec<Filter> CODEC = Codec.either(
        Codec.either(CodecMapCodec.dispatch(PsuedoRegisters.FILTER_TYPES, "filter", true),
            ResourceLocation.CODEC.<IsFilter>xmap({new IsFilter(it)}, {it.value})).<Filter>xmap({
        it.map({it},{it})
    },{ it instanceof IsFilter ? Either.right(it) : Either.left(it)}),
    Codec.STRING.<TagFilter>flatXmap({
        if (it.startsWith('#')) {
            ResourceLocation.read(it.substring(1)).map({new TagFilter(it)})
        }
        return DataResult.<TagFilter>error("Not a tag filter: ${it}")
    },{
        return DataResult.<String>success("#${it.value}")
    })).<Filter>xmap({
        it.map({ it }, { it })
    },{ it instanceof TagFilter ? Either.right(it) : Either.left(it)})

    abstract <T> boolean matches(T thing, FilterFinder<T> checker)

    static FilterFinder<ResourceLocation> simpleFinder() {
        return new FilterFinder<ResourceLocation>() {
            @Override
            boolean isTag(ResourceLocation thing, ResourceLocation tag) {
                return false
            }

            @Override
            boolean isLocation(ResourceLocation thing, ResourceLocation location) {
                return thing == location
            }
        }
    }
}
