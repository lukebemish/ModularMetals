package io.github.lukebemish.modularmetals.data.filter

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import groovy.transform.CompileStatic
import groovy.transform.KnownImmutable
import io.github.groovymc.cgl.api.transform.codec.ExposeCodec
import io.github.lukebemish.modularmetals.PsuedoRegisters
import io.github.lukebemish.modularmetals.util.CodecAware
import io.github.lukebemish.modularmetals.util.CodecMapCodec
import net.minecraft.resources.ResourceLocation

@CompileStatic
@KnownImmutable
abstract class Filter implements CodecAware {
    @ExposeCodec
    static final Codec<Filter> CODEC = Codec.either(CodecMapCodec.dispatch(PsuedoRegisters.FILTER_TYPES, "filter"),
            ResourceLocation.CODEC.<IsFilter>xmap({new IsFilter(it)}, {it.value})).<Filter>xmap({
        it.map({it},{it})
    },{ it instanceof IsFilter ? Either.right(it) : Either.left(it)})

    abstract boolean matches(ResourceLocation rl)
}
