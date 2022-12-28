package io.github.lukebemish.modularmetals.data.texsources

import com.google.gson.JsonSyntaxException
import com.mojang.blaze3d.platform.NativeImage
import com.mojang.serialization.Codec
import dev.lukebemish.dynamicassetgenerator.api.ResourceGenerationContext
import dev.lukebemish.dynamicassetgenerator.api.client.generators.ITexSource
import dev.lukebemish.dynamicassetgenerator.api.client.generators.TexSourceDataHolder
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import net.minecraft.server.packs.resources.IoSupplier
import org.jetbrains.annotations.NotNull

import java.util.function.Supplier

@CompileStatic
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

    @TupleConstructor
    static class TemplateData {
        final Map<String,ITexSource> templates
        final String defaultName
        TexSourceDataHolder dataHolderOverride = null
    }
}
