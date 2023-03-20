package io.github.lukebemish.modularmetals.data.texsources


import com.mojang.blaze3d.platform.NativeImage
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import dev.lukebemish.dynamicassetgenerator.api.ResourceGenerationContext
import dev.lukebemish.dynamicassetgenerator.api.client.generators.ITexSource
import dev.lukebemish.dynamicassetgenerator.api.client.generators.TexSourceDataHolder
import dev.lukebemish.dynamicassetgenerator.api.client.generators.texsources.ErrorSource
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import io.github.lukebemish.modularmetals.data.Metal
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.IoSupplier

@TupleConstructor
@CodecSerializable
class PropertyOrDefaultSource implements ITexSource {
    final ResourceLocation property
    final ITexSource backup
    final ITexSource source

    @Override
    Codec<? extends ITexSource> codec() {
        return $CODEC
    }

    @Override
    IoSupplier<NativeImage> getSupplier(TexSourceDataHolder data, ResourceGenerationContext context) {
        return {->
            PropertyGetterData getter = data.get(PropertyGetterData)
            if (getter === null)
                data.logger.error("No metal property source attached! Are you using this outside of a modularmetals config?")
            ITexSource propertySource = getter?.getSourceFromProperty(property)
            if (propertySource === null) {
                return backup.getSupplier(data, context).get()
            } else {
                var capture = new MostRecentCapturedProperty(propertySource)
                TexSourceDataHolder newData = new TexSourceDataHolder(data)
                newData.put(MostRecentCapturedProperty, capture)
                return source.getSupplier(newData, context).get()
            }
        }
    }

    @Override
    <T> DataResult<T> cacheMetadata(DynamicOps<T> ops, TexSourceDataHolder data) {
        PropertyGetterData getter = data.get(PropertyGetterData.class)
        ITexSource propertySource
        if (getter !== null && (propertySource=getter.getSourceFromProperty(property)) !== null) {
            var builder = ops.mapBuilder()
            builder.add('from_property', ITexSource.CODEC.encodeStart(ops, propertySource))
            return builder.build(ops.empty())
        }
        return DataResult.error {->'Could not get or encode property-based texture source'}
    }

    @TupleConstructor
    static class PropertyGetterData {
        Metal metal
        ResourceLocation metalLocation

        ITexSource getSourceFromProperty(ResourceLocation location) {
            var result = metal.getPropertyFromMap(location)?.decode(ITexSource.CODEC)
            return result?.result()?.orElseGet {->new ErrorSource("Failed to parse property ${location} as texture source for metal ${metalLocation}: ${result.error().get().message()}")}
        }
    }

    @TupleConstructor
    static class MostRecentCapturedProperty {
        ITexSource property
    }
}
