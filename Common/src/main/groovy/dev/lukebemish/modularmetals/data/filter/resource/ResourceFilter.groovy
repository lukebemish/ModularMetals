package dev.lukebemish.modularmetals.data.filter.resource

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import groovy.transform.KnownImmutable
import io.github.groovymc.cgl.api.transform.codec.ExposeCodec
import dev.lukebemish.modularmetals.PseudoRegisters
import dev.lukebemish.modularmetals.util.CodecAware
import dev.lukebemish.modularmetals.util.CodecMapCodec
import net.minecraft.resources.ResourceLocation

@KnownImmutable
abstract class ResourceFilter implements CodecAware {
    @ExposeCodec
    static final Codec<ResourceFilter> CODEC = Codec.either(
        Codec.either(CodecMapCodec.dispatch(PseudoRegisters.RESOURCE_FILTER_TYPES, "filter", true),
            ResourceLocation.CODEC.<IsResourceFilter>xmap({new IsResourceFilter(it)}, {it.value})).<ResourceFilter>xmap({
        it.map({it},{it})
    },{ it instanceof IsResourceFilter ? Either.right(it) : Either.left(it)}),
    Codec.STRING.<TagResourceFilter>flatXmap({
        if (it.startsWith('#')) {
            ResourceLocation.read(it.substring(1)).map({new TagResourceFilter(it)})
        }
        return DataResult.<TagResourceFilter>error {->"Not a tag filter: ${it}"}
    },{
        return DataResult.<String>success("#${it.value}")
    })).<ResourceFilter>xmap({
        it.map({ it }, { it })
    },{ it instanceof TagResourceFilter ? Either.right(it) : Either.left(it)})

    abstract <T> boolean matches(T thing, ResourceFilterFinder<T> checker)

    public static final ResourceFilterFinder<ResourceLocation> SIMPLE_FINDER = new ResourceFilterFinder<ResourceLocation>() {
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
