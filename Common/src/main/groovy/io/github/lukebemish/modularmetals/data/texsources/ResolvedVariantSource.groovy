package io.github.lukebemish.modularmetals.data.texsources


import com.mojang.blaze3d.platform.NativeImage
import com.mojang.serialization.Codec
import dev.lukebemish.dynamicassetgenerator.api.ResourceGenerationContext
import dev.lukebemish.dynamicassetgenerator.api.client.generators.ITexSource
import dev.lukebemish.dynamicassetgenerator.api.client.generators.TexSourceDataHolder
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import net.minecraft.server.packs.resources.IoSupplier

import java.util.function.Supplier

@CompileStatic
@Singleton
class ResolvedVariantSource implements ITexSource {
    @io.github.groovymc.cgl.api.transform.codec.ExposeCodec
    static final Codec<ResolvedVariantSource> CODEC = Codec.unit({-> instance} as Supplier)

    @Override
    Codec<? extends ITexSource> codec() {
        return CODEC
    }

    @Override
    IoSupplier<NativeImage> getSupplier(TexSourceDataHolder data, ResourceGenerationContext context) {
        ResolvedVariantData variant = data.get(ResolvedVariantData.class)
        if (variant===null) {
            data.logger.error("No provided resolved variant to capture... Are you trying to use the resolved variant source outside of a Modular Metals variant config?")
            return null
        }
        return {->
            return variant.template.getSupplier(data, context).get()
        }
    }

    @TupleConstructor
    static class ResolvedVariantData {
        final ITexSource template
    }
}
