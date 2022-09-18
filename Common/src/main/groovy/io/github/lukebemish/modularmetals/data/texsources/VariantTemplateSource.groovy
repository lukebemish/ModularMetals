package io.github.lukebemish.modularmetals.data.texsources

import com.google.gson.JsonSyntaxException
import com.mojang.blaze3d.platform.NativeImage
import com.mojang.serialization.Codec
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import io.github.lukebemish.dynamic_asset_generator.api.client.generators.ITexSource
import io.github.lukebemish.dynamic_asset_generator.api.client.generators.TexSourceDataHolder
import io.github.lukebemish.groovyduvet.wrapper.minecraft.api.codec.CodecSerializable
import org.jetbrains.annotations.NotNull

import java.util.function.Supplier

@CompileStatic
@CodecSerializable(property = 'CODEC')
@TupleConstructor
class VariantTemplateSource implements ITexSource {
    final Optional<String> template

    @Override
    Codec<? extends ITexSource> codec() {
        return CODEC
    }

    @Override
    @NotNull
    Supplier<NativeImage> getSupplier(TexSourceDataHolder data) throws JsonSyntaxException {
        return {
            TemplateData variant = data.get(TemplateData.class)
            if (variant===null) {
                data.logger.error("No provided variant template to capture... Are you trying to use the variant template source outside of a Modular Metals metal config?")
                return null
            }
            String templateName = template.orElse(variant.defaultName)
            TexSourceDataHolder templateData = variant.dataHolderOverride?:data
            return variant.templates.get(templateName)?.getSupplier(templateData)?.get()
        }
    }

    @TupleConstructor
    static class TemplateData {
        final Map<String,ITexSource> templates
        final String defaultName
        TexSourceDataHolder dataHolderOverride = null
    }
}
