package io.github.lukebemish.modularmetals.data

import com.google.gson.JsonElement
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import groovy.transform.CompileStatic
import groovy.transform.Immutable
import io.github.lukebemish.groovyduvet.wrapper.minecraft.api.codec.ExposeCodec
import io.github.lukebemish.groovyduvet.wrapper.minecraft.api.codec.ObjectOps
import io.github.lukebemish.modularmetals.util.OpsCodec

@Immutable(knownImmutableClasses = [JsonElement])
@CompileStatic
class MapHolder {
    @ExposeCodec
    static final Codec<MapHolder> CODEC = new OpsCodec<>(ObjectOps.instance).<MapHolder>xmap({new MapHolder((Map) it)}, {it.map})

    final Map map

    <O> DataResult<O> decode(Codec<? extends O> codec) {
        return codec.parse(ObjectOps.instance, map)
    }
}
