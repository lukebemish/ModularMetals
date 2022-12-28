package io.github.lukebemish.modularmetals.data.texsources

import com.google.gson.JsonSyntaxException
import com.mojang.blaze3d.platform.NativeImage
import com.mojang.serialization.Codec
import dev.lukebemish.dynamicassetgenerator.api.ResourceGenerationContext
import dev.lukebemish.dynamicassetgenerator.api.client.generators.ITexSource
import dev.lukebemish.dynamicassetgenerator.api.client.generators.TexSourceDataHolder
import dev.lukebemish.dynamicassetgenerator.api.client.generators.texsources.*
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.IoSupplier

import java.util.function.Supplier

@CodecSerializable
@TupleConstructor
class EasyRecolorSource implements ITexSource {
    final List<Integer> color

    @Override
    Codec<? extends ITexSource> codec() {
        return $CODEC
    }

    @Override
    IoSupplier<NativeImage> getSupplier(TexSourceDataHolder data, ResourceGenerationContext context) {
        ITexSource internal = new AnimationSplittingSource(
                ['template':new AnimationSplittingSource.TimeAwareSource(
                        new VariantTemplateSource(Optional.empty()),
                        1
                )],
                new CombinedPaletteImage(
                        new TextureReader(new ResourceLocation('dynamic_asset_generator','empty')),
                        new ColorSource(color),
                        new AnimationFrameCapture('template'),
                        false, false, 0
                )
        )
        return internal.getSupplier(data, context)
    }
}
