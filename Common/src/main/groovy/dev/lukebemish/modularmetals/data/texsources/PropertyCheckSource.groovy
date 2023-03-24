package dev.lukebemish.modularmetals.data.texsources

import com.mojang.blaze3d.platform.NativeImage
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import dev.lukebemish.dynamicassetgenerator.api.ResourceGenerationContext
import dev.lukebemish.dynamicassetgenerator.api.client.generators.ITexSource
import dev.lukebemish.dynamicassetgenerator.api.client.generators.TexSourceDataHolder
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.IoSupplier

@TupleConstructor
@CodecSerializable
class PropertyCheckSource implements ITexSource {
    final ResourceLocation property
    final ITexSource present
    final ITexSource absent

    @Override
    Codec<? extends ITexSource> codec() {
        return $CODEC
    }

    @Override
    IoSupplier<NativeImage> getSupplier(TexSourceDataHolder data, ResourceGenerationContext context) {
        return {->
            PropertyOrDefaultSource.PropertyGetterData getter = data.get(PropertyOrDefaultSource.PropertyGetterData)
            if (getter === null)
                data.logger.error("No metal property source attached! Are you using this outside of a modularmetals config?")
            ITexSource propertySource = getter?.getSourceFromProperty(property)
            if (propertySource === null) {
                return absent.getSupplier(data, context).get()
            } else {
                return present.getSupplier(data, context).get()
            }
        }
    }

    @Override
    <T> DataResult<T> cacheMetadata(DynamicOps<T> ops, TexSourceDataHolder data) {
        return DataResult.error {->'Property-checking source is not cacheable'}
    }
}
