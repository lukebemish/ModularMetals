package io.github.lukebemish.modularmetals.data.texsources

import com.google.gson.JsonSyntaxException
import com.mojang.blaze3d.platform.NativeImage
import com.mojang.serialization.Codec
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import io.github.lukebemish.dynamic_asset_generator.api.client.generators.ITexSource
import io.github.lukebemish.dynamic_asset_generator.api.client.generators.TexSourceDataHolder
import io.github.lukebemish.dynamic_asset_generator.api.client.generators.texsources.AnimationFrameCapture
import io.github.lukebemish.dynamic_asset_generator.api.client.generators.texsources.AnimationSplittingSource
import io.github.lukebemish.dynamic_asset_generator.api.client.generators.texsources.ColorSource
import io.github.lukebemish.dynamic_asset_generator.api.client.generators.texsources.CombinedPaletteImage
import io.github.lukebemish.dynamic_asset_generator.api.client.generators.texsources.TextureReader
import io.github.lukebemish.groovyduvet.wrapper.minecraft.api.codec.CodecSerializable
import net.minecraft.resources.ResourceLocation

import java.util.function.Supplier

@CompileStatic
@CodecSerializable
@TupleConstructor
class EasyRecolorSource implements ITexSource {
    final List<Integer> color

    @Override
    Codec<? extends ITexSource> codec() {
        return $CODEC
    }

    @Override
    Supplier<NativeImage> getSupplier(TexSourceDataHolder data) throws JsonSyntaxException {
        ITexSource internal = new AnimationSplittingSource(
                ['template':new AnimationSplittingSource.TimeAwareSource(
                        VariantTemplateSource.instance,
                        1
                )],
                new CombinedPaletteImage(
                        new TextureReader(new ResourceLocation('dynamic_asset_generator','empty')),
                        new ColorSource(color),
                        new AnimationFrameCapture('template'),
                        false, true, 0
                )
        )
        return internal.getSupplier(data)
    }
}
