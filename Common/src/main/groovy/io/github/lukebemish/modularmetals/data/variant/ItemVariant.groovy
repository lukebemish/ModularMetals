package io.github.lukebemish.modularmetals.data.variant

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import io.github.lukebemish.modularmetals.Constants
import io.github.lukebemish.modularmetals.ModularMetalsCommon
import io.github.lukebemish.modularmetals.data.MapHolder
import io.github.lukebemish.modularmetals.data.Metal
import io.github.lukebemish.modularmetals.services.Services
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item

@CompileStatic
@CodecSerializable
@TupleConstructor(includeSuperProperties = true, callSuper = true, includeFields = true)
class ItemVariant extends Variant {
    protected ItemVariantTexturing texturing
    final String name
    final Optional<List<String>> tags
    final Optional<List<String>> requiredMods

    @Override
    Codec getCodec() {
        return $CODEC
    }

    ItemVariantTexturing getTexturing() {
        return this.@texturing
    }

    @TupleConstructor
    @CodecSerializable
    static class ItemVariantTexturing {
        final Optional<Either<MapHolder,Map<String,MapHolder>>> generator
        final Optional<Either<MapHolder,Map<String,MapHolder>>> model
        final Either<ResourceLocation,Map<String,Either<ResourceLocation,MapHolder>>> template
    }

    void register(Metal metal, ResourceLocation metalLocation, ResourceLocation variantLocation) {
        if (requiredMods.orElse([]).every {Services.PLATFORM.isModPresent(it)}) {
            String location = ModularMetalsCommon.assembleMetalVariantName(metalLocation, variantLocation).path
            registerItem(location, variantLocation, metalLocation, metal)
        }
    }

    void registerItem(String location, ResourceLocation variantRl, ResourceLocation metalRl, Metal metal) {
        getItemTags(metalRl).each {
            ModularMetalsCommon.DATA_CACHE.tags().queue(new ResourceLocation(it.namespace, "items/${it.path}"), new ResourceLocation(Constants.MOD_ID, location))
        }
        ModularMetalsCommon.ITEMS.register(location, {->new Item(new Item.Properties())})
    }

    List<ResourceLocation> getItemTags(ResourceLocation metalRl) {
        return (tags.orElse([])).collect {ResourceLocation.of(it.replaceAll(/%s/, metalRl.path), ':' as char)}
    }
}
