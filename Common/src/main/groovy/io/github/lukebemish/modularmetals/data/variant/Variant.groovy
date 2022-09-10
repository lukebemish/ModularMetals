package io.github.lukebemish.modularmetals.data.variant


import com.mojang.serialization.Codec
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import io.github.lukebemish.groovyduvet.wrapper.minecraft.api.codec.ExposeCodec
import io.github.lukebemish.modularmetals.PsuedoRegisters
import io.github.lukebemish.modularmetals.data.Metal
import io.github.lukebemish.modularmetals.data.ModConfig
import io.github.lukebemish.modularmetals.util.CodecAware
import net.minecraft.resources.ResourceLocation

@CompileStatic
@TupleConstructor
abstract class Variant implements CodecAware {
    @ExposeCodec
    static final Codec<Variant> CODEC = ModConfig.dispatchedToDefaultResources(PsuedoRegisters.VARIANT_TYPES, 'variant','variants')

    final Optional<Boolean> defaultEnabled

    abstract void register(Metal metal, ResourceLocation metalLocation, ResourceLocation variantLocation)
}
