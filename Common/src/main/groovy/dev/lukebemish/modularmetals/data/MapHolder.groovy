package dev.lukebemish.modularmetals.data


import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import groovy.transform.CompileStatic
import io.github.groovymc.cgl.api.codec.ObjectOps
import io.github.groovymc.cgl.api.transform.codec.ExposeCodec
import dev.lukebemish.modularmetals.util.OpsCodec

@CompileStatic
class MapHolder {
    @ExposeCodec
    static final Codec<MapHolder> CODEC = new OpsCodec<>(ObjectOps.instance).<MapHolder>flatXmap({
        if (it instanceof Map)
            return DataResult.success(new MapHolder((Map) it))
        return DataResult.<MapHolder>error {->"Expected map, got something else"}
    }, {
        DataResult.success(it.map)
    })

    final Map map

    MapHolder(Map map) {
        this.map = map
    }

    <O> DataResult<O> decode(Codec<? extends O> codec) {
        return codec.parse(ObjectOps.instance, map)
    }
}
