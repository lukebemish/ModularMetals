package io.github.lukebemish.modularmetals.data.texsources

import com.google.gson.JsonSyntaxException
import com.mojang.blaze3d.platform.NativeImage
import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import io.github.lukebemish.dynamic_asset_generator.api.client.generators.ITexSource
import io.github.lukebemish.dynamic_asset_generator.api.client.generators.TexSourceDataHolder
import io.github.lukebemish.groovyduvet.wrapper.minecraft.api.codec.CodecSerializable

import java.util.function.Supplier

@TupleConstructor
@CodecSerializable
@CompileStatic
class WithTemplateSource implements ITexSource {
    final Either<ITexSource,Map<String,ITexSource>> template
    final ITexSource source

    @Override
    Codec<? extends ITexSource> codec() {
        return $CODEC
    }

    @Override
    Supplier<NativeImage> getSupplier(TexSourceDataHolder data) throws JsonSyntaxException {
        TexSourceDataHolder newData = new TexSourceDataHolder(data)
        String name = data.get(VariantTemplateSource.TemplateData)?.defaultName
        if (name === null)
            name = ''
        newData.put(VariantTemplateSource.TemplateData, new VariantTemplateSource.TemplateData(template.<Map<String,ITexSource>>map({Map.of(name, it)},{it}), name).tap {
            it.dataHolderOverride = data
        })
        return source.getSupplier(newData)
    }
}
