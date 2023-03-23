package io.github.lukebemish.modularmetals.data

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.codec.ObjectOps
import io.github.groovymc.cgl.api.transform.codec.ExposeCodecFactory
import io.github.lukebemish.modularmetals.TemplateEngine
import org.jetbrains.annotations.Nullable

import java.util.function.Function

abstract class Fillable<T> implements Function<Map, DataResult<T>> {

    abstract DataResult<ObjectHolder> getMap()

    @ExposeCodecFactory
    static <T> Codec<Fillable<T>> codec(Codec<T> codec) {
        return ObjectHolder.CODEC.<Fillable<T>>flatXmap({
            DataResult.success(Fillable.<T>of(it, codec))
        }, {
            it.map
        })
    }

    static <T> Fillable<T> ofValue(T value, Codec<T> codec) {
        return new ValueFillable<T>(value, codec)
    }

    static <T> Fillable<T> of(ObjectHolder initial, Codec<T> codec) {
        return new MapFillable<T>(initial, codec)
    }

    @TupleConstructor
    static final class MapFillable<T> extends Fillable<T> {
        final ObjectHolder initial
        final Codec<T> codec

        @Override
        @Nullable
        DataResult<T> apply(Map replacements) {
            Object map = TemplateEngine.fillReplacements(initial.obj, replacements)
            return codec.parse(ObjectOps.instance, map)
        }

        @Override
        DataResult<ObjectHolder> getMap() {
            return DataResult.success(initial)
        }
    }

    @TupleConstructor
    static final class ValueFillable<T> extends Fillable<T> {
        final T value
        final Codec<T> codec

        @Override
        DataResult<T> apply(Map map) {
            return DataResult.success(value)
        }

        @Override
        DataResult<ObjectHolder> getMap() {
            return codec.encodeStart(ObjectOps.instance, value).<ObjectHolder>flatMap({
                return ObjectHolder.CODEC.parse(ObjectOps.instance, it)
            })
        }
    }
}
