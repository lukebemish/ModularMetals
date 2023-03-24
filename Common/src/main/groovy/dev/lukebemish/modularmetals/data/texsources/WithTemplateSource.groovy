package dev.lukebemish.modularmetals.data.texsources

import com.google.gson.JsonSyntaxException
import com.mojang.blaze3d.platform.NativeImage
import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import dev.lukebemish.dynamicassetgenerator.api.ResourceGenerationContext
import dev.lukebemish.dynamicassetgenerator.api.client.generators.ITexSource
import dev.lukebemish.dynamicassetgenerator.api.client.generators.TexSourceDataHolder
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import net.minecraft.server.packs.resources.IoSupplier

import java.util.function.Supplier

@TupleConstructor
@CodecSerializable
class WithTemplateSource implements ITexSource {
    final Either<ITexSource,Map<String,ITexSource>> template
    final ITexSource source

    @Override
    Codec<? extends ITexSource> codec() {
        return $CODEC
    }

    @Override
    IoSupplier<NativeImage> getSupplier(TexSourceDataHolder data, ResourceGenerationContext context) {
        TexSourceDataHolder newData = new TexSourceDataHolder(data)
        String name = data.get(VariantTemplateSource.TemplateData)?.defaultName
        if (name === null)
            name = ''
        newData.put(VariantTemplateSource.TemplateData, new VariantTemplateSource.TemplateData(template.<Map<String,ITexSource>>map({Map.of(name, it)},{it}), name).tap {
            it.dataHolderOverride = data
        })
        return source.getSupplier(newData, context)
    }
}
