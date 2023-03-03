package io.github.lukebemish.modularmetals.data.texsources


import com.mojang.blaze3d.platform.NativeImage
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import dev.lukebemish.dynamicassetgenerator.api.ResourceGenerationContext
import dev.lukebemish.dynamicassetgenerator.api.client.generators.ITexSource
import dev.lukebemish.dynamicassetgenerator.api.client.generators.TexSourceDataHolder
import dev.lukebemish.dynamicassetgenerator.api.client.generators.texsources.*
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.IoSupplier

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
        ITexSource internal = this.internal
        return internal.getSupplier(data, context)
    }

    @Override
    <T> DataResult<T> cacheMetadata(DynamicOps<T> ops, TexSourceDataHolder data) {
        ITexSource internal = this.internal
        var builder = ops.mapBuilder()
        builder.add('constructed', ITexSource.CODEC.encodeStart(ops, internal))

        return builder.build(ops.empty())
    }

    private ITexSource getInternal() {
        return new AnimationSplittingSource(
            ['easy_recolor_template':new AnimationSplittingSource.TimeAwareSource(
                new VariantTemplateSource(Optional.empty()),
                1
            )],
            new CombinedPaletteImage(
                new TextureReader(new ResourceLocation('dynamic_asset_generator','empty')),
                new ColorSource(color),
                new AnimationFrameCapture('easy_recolor_template'),
                false, false, 0
            )
        )
    }
}
