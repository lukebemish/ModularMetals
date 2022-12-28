package io.github.lukebemish.modularmetals.data

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.codec.ObjectOps
import io.github.groovymc.cgl.api.transform.codec.ExposeCodec
import io.github.lukebemish.modularmetals.util.OpsCodec

@TupleConstructor
class ObjectHolder {
    @ExposeCodec
    static final Codec<ObjectHolder> CODEC = new OpsCodec<>(ObjectOps.instance).<ObjectHolder>xmap({new ObjectHolder(it)}, {it.obj})

    final Object obj

    <O> DataResult<O> decode(Codec<? extends O> codec) {
        return codec.parse(ObjectOps.instance, obj)
    }
}
