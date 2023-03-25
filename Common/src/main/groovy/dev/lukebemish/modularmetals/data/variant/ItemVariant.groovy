package dev.lukebemish.modularmetals.data.variant

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import dev.lukebemish.modularmetals.Constants
import dev.lukebemish.modularmetals.ModularMetalsCommon
import dev.lukebemish.modularmetals.client.variant.ClientVariantHandler
import dev.lukebemish.modularmetals.client.variant.ItemClientVariantHandler
import dev.lukebemish.modularmetals.data.Fillable
import dev.lukebemish.modularmetals.data.MapHolder
import dev.lukebemish.modularmetals.data.Metal
import dev.lukebemish.modularmetals.data.TexSourceMap
import dev.lukebemish.modularmetals.services.Services
import dev.lukebemish.modularmetals.util.MoreCodecs
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import io.github.groovymc.cgl.api.transform.codec.WithCodec
import io.github.groovymc.cgl.reg.RegistryObject
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.food.FoodProperties
import net.minecraft.world.item.Item
import net.minecraft.world.item.Rarity

import java.util.function.Function

@CodecSerializable
@TupleConstructor(includeSuperProperties = true, callSuper = true, includeFields = true)
@CompileStatic
class ItemVariant extends Variant {
    protected ItemVariantTexturing texturing
    final Either<String,Map<String,String>> name
    final Optional<Fillable<List<String>>> tags
    final Optional<Fillable<ItemPropertiesBuilder>> itemProperties

    @CodecSerializable
    @TupleConstructor
    static class ItemPropertiesBuilder {
        final int maxStackSize = 64
        final int maxDamage = 0
        final boolean fireResistant = false
        final Optional<ItemFoodPropertiesBuilder> foodProperties = Optional.empty()
        @WithCodec({ MoreCodecs.RARITY_CODEC })
        final Rarity rarity = Rarity.COMMON

        Item.Properties makeProperties() {
            var props = new Item.Properties()
                .stacksTo(maxStackSize)
                .durability(maxDamage)
                .rarity(rarity)
            if (fireResistant) props = props.fireResistant()
            foodProperties.ifPresent { props = props.food(it.makeProperties()) }
            return props
        }

        @CodecSerializable
        @TupleConstructor
        static class ItemFoodPropertiesBuilder {
            final int nutrition
            final int saturation
            final boolean meat
            final boolean fast
            final boolean alwaysEat

            FoodProperties makeProperties() {
                var builder = new FoodProperties.Builder()
                    .nutrition(nutrition)
                    .saturationMod(saturation)
                if (meat) builder = builder.meat()
                if (fast) builder = builder.fast()
                if (alwaysEat) builder = builder.alwaysEat()
                return builder.build()
            }
        }
    }

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

    void register(Metal metal, ResourceLocation metalLocation, ResourceLocation variantLocation, Map<ResourceLocation, ResourceLocation> variantLocations) {
        String location = ModularMetalsCommon.assembleMetalVariantName(metalLocation, variantLocation).path
        Map props = fillProperties(new ResourceLocation(Constants.MOD_ID, location), metalLocation, metal, variantLocations)
        var item = registerItem(location, variantLocation, metalLocation, metal, props)
        Services.PLATFORM.addTabItem {item.get().defaultInstance}
        getTags(metalLocation, props).each {
            ModularMetalsCommon.DATA_CACHE.tags().queue(new ResourceLocation(it.namespace, "items/${it.path}"), new ResourceLocation(Constants.MOD_ID, location))
        }
    }

    @Override
    ClientVariantHandler getClientHandler() {
        return new ItemClientVariantHandler()
    }

    String makeTranslationKey(String path) {
        return "item.${Constants.MOD_ID}.${path}"
    }

    RegistryObject<? extends Item> registerItem(String location, ResourceLocation variantRl, ResourceLocation metalRl, Metal metal, Map props) {
        return ModularMetalsCommon.ITEMS.register(location, {->new Item(makeProperties(props))})
    }

    Item.Properties makeProperties(Map props) {
        return itemProperties
            .flatMap {it.apply(props).result()}
            .map { it.makeProperties() }
            .orElse(new Item.Properties())
    }

    List<ResourceLocation> getTags(ResourceLocation metalRl, Map props) {
        return (tags.flatMap {it.apply(props).result()}.orElse([])).collect {ResourceLocation.of(it.replaceAll(/%s/, metalRl.path), ':' as char)}
    }
}
