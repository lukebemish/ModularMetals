package io.github.lukebemish.modularmetals.data.texsources


import com.mojang.blaze3d.platform.NativeImage
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import dev.lukebemish.dynamicassetgenerator.api.ResourceGenerationContext
import dev.lukebemish.dynamicassetgenerator.api.client.generators.ITexSource
import dev.lukebemish.dynamicassetgenerator.api.client.generators.TexSourceDataHolder
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import net.minecraft.server.packs.resources.IoSupplier

@CodecSerializable
@TupleConstructor
class VariantTemplateSource implements ITexSource {
    final Optional<String> template

    @Override
    Codec<? extends ITexSource> codec() {
        return $CODEC
    }

    @Override
    IoSupplier<NativeImage> getSupplier(TexSourceDataHolder data, ResourceGenerationContext context) {
        TemplateData variant = data.get(TemplateData.class)
        if (variant===null) {
            data.logger.error("No provided variant template to capture... Are you trying to use the variant template source outside of a Modular Metals metal config?")
            return null
        }
        String templateName = template.orElse(variant.defaultName)
        TexSourceDataHolder templateData = variant.dataHolderOverride?:data
        return variant.templates.get(templateName)?.getSupplier(templateData, context)
    }

    @Override
    <T> DataResult<T> cacheMetadata(DynamicOps<T> ops, TexSourceDataHolder data) {
        TemplateData templateData = data.get(TemplateData.class)
        if (templateData != null) {
            String templateName = template.orElse(templateData.defaultName)
            if (templateData.dataHolderOverride != null) {
                return DataResult.error('Could not cache variant template source due to changing data holder')
            }
            var builder = ops.mapBuilder()
            ITexSource source = templateData.templates.get(templateName)
            if (source == null) {
                return super.cacheMetadata(ops, data)
            }
            builder.add('template', ITexSource.CODEC.encodeStart(ops, source))
            return builder.build(ops.empty())
        }
        return DataResult.error('Could not get or encode template data')
    }

    @TupleConstructor
    static class TemplateData {
        final Map<String,ITexSource> templates
        final String defaultName
        TexSourceDataHolder dataHolderOverride = null
    }
}
