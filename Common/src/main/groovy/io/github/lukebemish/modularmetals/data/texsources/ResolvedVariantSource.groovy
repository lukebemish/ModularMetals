package io.github.lukebemish.modularmetals.data.texsources

import com.mojang.blaze3d.platform.NativeImage
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import dev.lukebemish.dynamicassetgenerator.api.ResourceGenerationContext
import dev.lukebemish.dynamicassetgenerator.api.client.generators.ITexSource
import dev.lukebemish.dynamicassetgenerator.api.client.generators.TexSourceDataHolder
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.ExposeCodec
import net.minecraft.server.packs.resources.IoSupplier

import java.util.function.Supplier

@Singleton
class ResolvedVariantSource implements ITexSource {
    @ExposeCodec
    static final Codec<ResolvedVariantSource> RESOLVED_CODEC = Codec.unit({-> instance} as Supplier)

    @Override
    Codec<? extends ITexSource> codec() {
        return RESOLVED_CODEC
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

    @Override
    <T> DataResult<T> cacheMetadata(DynamicOps<T> ops, TexSourceDataHolder data) {
        ResolvedVariantData getter = data.get(ResolvedVariantData.class)
        if (getter != null) {
            var builder = ops.mapBuilder()
            builder.add('resolved', ITexSource.CODEC.encodeStart(ops, getter.template))
            return builder.build(ops.empty())
        }
        return DataResult.error('Could not get or encode resolved template')
    }

    @TupleConstructor
    static class ResolvedVariantData {
        final ITexSource template
    }
}
