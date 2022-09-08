package io.github.lukebemish.modularmetals.util

import com.google.common.collect.BiMap
import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.ExtraCodecs

import java.util.function.Function

@CompileStatic
@TupleConstructor
class CodecMapCodec<O extends CodecAware<O>> implements Codec<Codec<? extends O>> {
    final BiMap<ResourceLocation, Codec<? extends O>> lookup
    final String name

    @Override
    <T> DataResult<T> encode(Codec<? extends O> input, DynamicOps<T> ops, T prefix) {
        ResourceLocation key = lookup.inverse().get(input)
        if (key == null)
            return DataResult.error("Unregistered ${name} type: ${input}")
        T toMerge = ops.createString(key.toString())
        return ops.mergeToPrimitive(prefix, toMerge)
    }

    @Override
    <T> DataResult<Pair<Codec<? extends O>, T>> decode(DynamicOps<T> ops, T input) {
        return ResourceLocation.CODEC.decode(ops, input).flatMap { !lookup.containsKey(it.getFirst())
                ? DataResult.<Pair<Codec<? extends O>, T>>error("Unknown ${name} type: " + it.getFirst())
                : DataResult.<Pair<Codec<? extends O>, T>>success(it.mapFirst {lookup.get(it)})
        }
    }

    static <T extends CodecAware<T>> Codec<T> dispatch(BiMap<ResourceLocation, Codec<? extends T>> lookup, String name) {
        return ExtraCodecs.<Codec<? extends T>>lazyInitializedCodec {->
            new CodecMapCodec<T>(lookup, name)
        }.dispatch({CodecAware it -> it.codec}, Function.identity())
    }
}
