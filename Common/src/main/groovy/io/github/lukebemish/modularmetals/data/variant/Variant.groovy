package io.github.lukebemish.modularmetals.data.variant

import com.mojang.serialization.Codec
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.ExposeCodec
import io.github.lukebemish.modularmetals.ModularMetalsCommon
import io.github.lukebemish.modularmetals.PsuedoRegisters
import io.github.lukebemish.modularmetals.client.variant.ClientVariantHandler
import io.github.lukebemish.modularmetals.data.Metal
import io.github.lukebemish.modularmetals.data.ModConfig
import io.github.lukebemish.modularmetals.util.CodecAware
import net.minecraft.resources.ResourceLocation

@TupleConstructor
abstract class Variant implements CodecAware {
    final Optional<List<String>> requiredMods
    @ExposeCodec
    static final Codec<Variant> CODEC = ModConfig.dispatchedToDefaultResources(PsuedoRegisters.VARIANT_TYPES, 'variant','variants')

    abstract void register(Metal metal, ResourceLocation metalLocation, ResourceLocation variantLocation, Map<ResourceLocation, ResourceLocation> variantLocations)

    abstract ClientVariantHandler getClientHandler()

    static Map fillProperties(ResourceLocation fullLocation, ResourceLocation metalLocation, Metal metal, Map<ResourceLocation, ResourceLocation> variantLocations) {
        Map replacements = ['location':fullLocation,'variants':variantLocations.keySet().toList().collectEntries {
            [it.toString(), variantLocations[it].toString()]
        },'metal':metalLocation,'properties':metal.properties.collectEntries {[it.key.toString(), it.value.obj]}]
        replacements += ModularMetalsCommon.sharedEnvMap
        return replacements
    }
}
