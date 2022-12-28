package io.github.lukebemish.modularmetals.util

import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.MapLike
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor

@TupleConstructor
class OpsCodec<O> implements Codec<O> {
    final DynamicOps<O> ops

    @Override
    <T> DataResult<Pair<O, T>> decode(DynamicOps<T> ops, T input) {
        try {
            O obj = ops.convertTo(this.ops, input)
            return DataResult.success(new Pair<>(obj, input))
        } catch (Exception e) {
            return DataResult.error(e.message)
        }
    }

    @Override
    <T> DataResult<T> encode(O input, DynamicOps<T> ops, T prefix) {
        try {
            T obj = this.ops.convertTo(ops, input)
            DataResult<T> out = ops.mergeToPrimitive(prefix, obj)
            if (out.error().present)
                out = ops.mergeToList(prefix, obj)
            if (out.error().present) {
                DataResult<MapLike<T>> map = ops.getMap(prefix)
                if (!map.error().present) {
                    out = ops.mergeToMap(prefix, map.getOrThrow(false, {}))
                }
            }
            return out
        } catch (Exception e) {
            return DataResult.error(e.message)
        }
    }
}
