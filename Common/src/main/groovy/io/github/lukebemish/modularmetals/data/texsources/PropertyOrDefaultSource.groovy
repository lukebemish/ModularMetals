package io.github.lukebemish.modularmetals.data.texsources

import com.google.gson.JsonSyntaxException
import com.mojang.blaze3d.platform.NativeImage
import com.mojang.serialization.Codec
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import io.github.lukebemish.dynamic_asset_generator.api.client.generators.ITexSource
import io.github.lukebemish.dynamic_asset_generator.api.client.generators.TexSourceDataHolder
import io.github.lukebemish.dynamic_asset_generator.api.client.generators.texsources.ErrorSource
import io.github.lukebemish.groovyduvet.wrapper.minecraft.api.codec.CodecSerializable
import io.github.lukebemish.modularmetals.data.Metal
import net.minecraft.resources.ResourceLocation

import java.util.function.Supplier

@TupleConstructor
@CodecSerializable
@CompileStatic
class PropertyOrDefaultSource implements ITexSource {
    final ResourceLocation property
    final ITexSource backup

    @Override
    Codec<? extends ITexSource> codec() {
        return $CODEC
    }

    @Override
    Supplier<NativeImage> getSupplier(TexSourceDataHolder data) throws JsonSyntaxException {
        return {->
            PropertyGetterData getter = data.get(PropertyGetterData)
            if (getter === null)
                data.logger.error("No metal property source attached! Are you using this outside of a modularmetals config?")
            Supplier<NativeImage> supplier = getter.getSourceFromProperty(property)?.getSupplier(data)?:backup.getSupplier(data)
            return supplier.get()
        }
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
}
