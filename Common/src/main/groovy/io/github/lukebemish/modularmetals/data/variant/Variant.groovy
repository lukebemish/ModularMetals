package io.github.lukebemish.modularmetals.data.variant


import com.mojang.serialization.Codec
import groovy.transform.CompileStatic
import io.github.lukebemish.groovyduvet.wrapper.minecraft.api.codec.ExposeCodec
import io.github.lukebemish.modularmetals.PsuedoRegisters
import io.github.lukebemish.modularmetals.data.ModConfig
import io.github.lukebemish.modularmetals.util.CodecAware

@CompileStatic
abstract class Variant implements CodecAware {
    @ExposeCodec
    static final Codec<Variant> CODEC = ModConfig.dispatchedToDefaultResources(PsuedoRegisters.VARIANT_TYPES, 'variant','variants')

    abstract boolean isEnabledByDefault()
}
