package io.github.lukebemish.modularmetals.data.variant

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import io.github.groovymc.cgl.reg.RegistryObject
import io.github.lukebemish.modularmetals.Constants
import io.github.lukebemish.modularmetals.ModularMetalsCommon
import io.github.lukebemish.modularmetals.client.variant.ClientVariantHandler
import io.github.lukebemish.modularmetals.client.variant.ItemClientVariantHandler
import io.github.lukebemish.modularmetals.data.MapHolder
import io.github.lukebemish.modularmetals.data.Metal
import io.github.lukebemish.modularmetals.data.TexSourceMap
import io.github.lukebemish.modularmetals.services.Services
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item

import java.util.function.Function

@CodecSerializable
@TupleConstructor(includeSuperProperties = true, callSuper = true, includeFields = true)
class ItemVariant extends Variant {
    protected ItemVariantTexturing texturing
    final Either<String,Map<String,String>> name
    final Optional<List<String>> tags

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
        final TexSourceMap template

        Optional<Map<String, MapHolder>> getSimplifiedGenerator() {
            return generator
                .<Map<String, MapHolder>>map(either -> either.<Map<String,MapHolder>>map(holder -> ['':holder], Function.identity()))
        }
    }

    void register(Metal metal, ResourceLocation metalLocation, ResourceLocation variantLocation) {
        String location = ModularMetalsCommon.assembleMetalVariantName(metalLocation, variantLocation).path
        var item = registerItem(location, variantLocation, metalLocation, metal)
        Services.PLATFORM.addTabItem {item.get().defaultInstance}
    }

    @Override
    ClientVariantHandler getClientHandler() {
        return new ItemClientVariantHandler()
    }

    String makeTranslationKey(String path) {
        return "item.${Constants.MOD_ID}.${path}"
    }

    RegistryObject<? extends Item> registerItem(String location, ResourceLocation variantRl, ResourceLocation metalRl, Metal metal) {
        getItemTags(metalRl).each {
            ModularMetalsCommon.DATA_CACHE.tags().queue(new ResourceLocation(it.namespace, "items/${it.path}"), new ResourceLocation(Constants.MOD_ID, location))
        }
        return ModularMetalsCommon.ITEMS.register(location, {->new Item(new Item.Properties())})
    }

    List<ResourceLocation> getItemTags(ResourceLocation metalRl) {
        return (tags.orElse([])).collect {ResourceLocation.of(it.replaceAll(/%s/, metalRl.path), ':' as char)}
    }
}
