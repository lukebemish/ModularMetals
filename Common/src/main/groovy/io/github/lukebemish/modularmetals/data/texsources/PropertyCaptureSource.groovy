package io.github.lukebemish.modularmetals.data.texsources

import com.mojang.blaze3d.platform.NativeImage
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import dev.lukebemish.dynamicassetgenerator.api.ResourceGenerationContext
import dev.lukebemish.dynamicassetgenerator.api.client.generators.ITexSource
import dev.lukebemish.dynamicassetgenerator.api.client.generators.TexSourceDataHolder
import net.minecraft.server.packs.resources.IoSupplier

@Singleton
class PropertyCaptureSource implements ITexSource {
    public static final Codec<PropertyCaptureSource> CODEC = Codec.unit {-> instance}

    @Override
    Codec<? extends ITexSource> codec() {
        return CODEC
    }

    @Override
    IoSupplier<NativeImage> getSupplier(TexSourceDataHolder data, ResourceGenerationContext context) {
        return {->
            PropertyOrDefaultSource.MostRecentCapturedProperty property = data.get(PropertyOrDefaultSource.MostRecentCapturedProperty)
            if (property === null)
                data.logger.error("No captured property source attached! Are you using this outside of a modularmetals config?")
            return property.property.getSupplier(data, context).get()
        }
    }

    @Override
    <T> DataResult<T> cacheMetadata(DynamicOps<T> ops, TexSourceDataHolder data) {
        PropertyOrDefaultSource.MostRecentCapturedProperty property = data.get(PropertyOrDefaultSource.MostRecentCapturedProperty)
        if (property !== null) {
            var builder = ops.mapBuilder()
            builder.add('captured_property', ITexSource.CODEC.encodeStart(ops, property.property))
            return builder.build(ops.empty())
        }
        return DataResult.error {->'Could not get or encode captured-property-based texture source'}
    }
}
