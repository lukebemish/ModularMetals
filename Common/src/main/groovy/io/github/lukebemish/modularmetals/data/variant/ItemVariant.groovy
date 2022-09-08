package io.github.lukebemish.modularmetals.data.variant

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.reg.RegistryObject
import io.github.lukebemish.groovyduvet.wrapper.minecraft.api.codec.CodecSerializable
import io.github.lukebemish.modularmetals.ModularMetalsCommon
import io.github.lukebemish.modularmetals.data.MapHolder
import io.github.lukebemish.modularmetals.data.Metal
import io.github.lukebemish.modularmetals.services.Services
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item

@CompileStatic
@CodecSerializable
@TupleConstructor
class ItemVariant extends Variant {
    final ItemVariantTexturing texturing
    final Optional<Boolean> default_enabled
    final String name
    final Optional<List<ResourceLocation>> tags

    @Override
    Codec getCodec() {
        return $CODEC
    }

    @Override
    boolean isEnabledByDefault() {
        return default_enabled.orElse(true)
    }

    @TupleConstructor
    @CodecSerializable
    static class ItemVariantTexturing {
        final Optional<Either<MapHolder,Map<String,MapHolder>>> generator
        final Optional<Either<MapHolder,Map<String,MapHolder>>> model
        final Either<ResourceLocation,Map<String,ResourceLocation>> template
    }

    RegistryObject<? extends Item> registerItem(String location, Metal metal, ResourceLocation metalRl) {
        return ModularMetalsCommon.ITEMS.register(location, {->new Item(new Item.Properties().tab(Services.PLATFORM.getItemTab()))})
    }

    List<ResourceLocation> getItemTags(ResourceLocation metalRl) {
        return (tags.orElse([])).collect {new ResourceLocation(it.namespace, it.path.replaceAll(/%s/, metalRl.path))}
    }
}
