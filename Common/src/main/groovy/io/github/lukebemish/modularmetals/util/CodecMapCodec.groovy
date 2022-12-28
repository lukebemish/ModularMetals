package io.github.lukebemish.modularmetals.util

import com.google.common.collect.BiMap
import com.mojang.datafixers.util.Either
import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.codec.ObjectOps
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

    static <O extends CodecAware<O>, T> Codec<O> dispatchWithInherit(BiMap<ResourceLocation, Codec<? extends O>> lookup, String name, Function<ResourceLocation, DataResult<T>> inheritanceFinder, DynamicOps<T> inheritanceOps) {
        Codec<O> dispatch = dispatch(lookup, name)
        var inheriting = new InheritingCodecHolder<O,T>(lookup, name, inheritanceFinder, inheritanceOps, dispatch)
        return inheriting.codec
    }

    static class InheritingCodecHolder<O extends CodecAware<O>,T> {
        final BiMap<ResourceLocation, Codec<? extends O>> lookup
        final String name
        final Function<ResourceLocation, DataResult<T>> inheritanceFinder
        final DynamicOps<T> inheritanceOps
        final Codec<O> dispatch
        final Codec<O> codec = Codec.either(Codec.pair(new OpsCodec<Object>(ObjectOps.instance),ResourceLocation.CODEC.fieldOf('inherit').codec()), dispatch).<O>flatXmap(
                {
                    it.<DataResult<O>>map({
                        return inheritanceFinder.apply(it.second).flatMap { data ->
                            Object map = inheritanceOps.convertTo(ObjectOps.instance, data)
                            if (map instanceof Map && it.first instanceof Map) {
                                return codec.decode(ObjectOps.instance, map + (Map) it.first).map { p -> p.first }
                            } else if (it.first instanceof Map)
                                return DataResult.error("Provided object not map-like: ${map}.")
                            else
                                return DataResult.error("Provided object not map-like: ${it.first}.")
                        }
                    },{
                        DataResult.success(it)
                    })
                },
                {
                    DataResult.success(Either.<Pair<Object,ResourceLocation>,O>right(it))
                }
        )
        protected InheritingCodecHolder(BiMap<ResourceLocation, Codec<? extends O>> lookup, String name, Function<ResourceLocation, DataResult<T>> inheritanceFinder, DynamicOps<T> inheritanceOps, Codec<O> dispatch) {
            this.lookup = lookup
            this.name = name
            this.inheritanceFinder = inheritanceFinder
            this.inheritanceOps = inheritanceOps
            this.dispatch = dispatch
        }
    }
}
