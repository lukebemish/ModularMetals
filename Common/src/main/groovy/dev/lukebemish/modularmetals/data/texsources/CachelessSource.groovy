package dev.lukebemish.modularmetals.data.texsources

import com.mojang.blaze3d.platform.NativeImage
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import dev.lukebemish.dynamicassetgenerator.api.ResourceGenerationContext
import dev.lukebemish.dynamicassetgenerator.api.client.generators.ITexSource
import dev.lukebemish.dynamicassetgenerator.api.client.generators.TexSourceDataHolder
import groovy.transform.TupleConstructor
import net.minecraft.server.packs.resources.IoSupplier
import org.jetbrains.annotations.Nullable

import java.util.function.Function

@TupleConstructor
class CachelessSource implements ITexSource {
    @Nullable Function<ResourceGenerationContext, NativeImage> imageSource
    public static final Codec<CachelessSource> CODEC = Codec.unit(new CachelessSource(null))

    @Override
    <T> DataResult<T> cacheMetadata(DynamicOps<T> ops, TexSourceDataHolder data) {
        return DataResult.<T>error {->'Cacheless source is not cacheable'}
    }

    @Override
    Codec<? extends ITexSource> codec() {
        return CODEC
    }

    @Override
    IoSupplier<NativeImage> getSupplier(TexSourceDataHolder data, ResourceGenerationContext context) {
        if (imageSource === null) return null
        return {->
            imageSource.apply(context)
        }
    }
}
