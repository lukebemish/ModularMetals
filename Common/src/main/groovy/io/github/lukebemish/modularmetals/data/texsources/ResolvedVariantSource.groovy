package io.github.lukebemish.modularmetals.data.texsources

import com.google.gson.JsonSyntaxException
import com.mojang.blaze3d.platform.NativeImage
import com.mojang.serialization.Codec
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import io.github.lukebemish.dynamic_asset_generator.api.client.generators.ITexSource
import io.github.lukebemish.dynamic_asset_generator.api.client.generators.TexSourceDataHolder
import io.github.lukebemish.groovyduvet.wrapper.minecraft.api.codec.ExposeCodec
import org.jetbrains.annotations.NotNull

import java.util.function.Supplier

@CompileStatic
@Singleton
class ResolvedVariantSource implements ITexSource {
    @ExposeCodec
    static final Codec<ResolvedVariantSource> CODEC = Codec.unit({-> instance} as Supplier)

    @Override
    Codec<? extends ITexSource> codec() {
        return CODEC
    }

    @Override
    @NotNull
    Supplier<NativeImage> getSupplier(TexSourceDataHolder data) throws JsonSyntaxException {
        return {->
            ResolvedVariantData variant = data.get(ResolvedVariantData.class)
            if (variant===null) {
                data.logger.error("No provided resolved variant to capture... Are you trying to use the resolved variant source outside of a Modular Metals variant config?")
                return null
            }
            return variant.template.getSupplier(data).get()
        }
    }

    @TupleConstructor
    static class ResolvedVariantData {
        final ITexSource template
    }
}
