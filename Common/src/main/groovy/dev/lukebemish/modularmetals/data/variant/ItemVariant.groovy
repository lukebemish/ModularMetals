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
import dev.lukebemish.modularmetals.data.MobEffectProvider
import dev.lukebemish.modularmetals.data.TexSourceMap
import dev.lukebemish.modularmetals.objects.MMItem
import dev.lukebemish.modularmetals.objects.MMItemProps
import dev.lukebemish.modularmetals.services.Services
import dev.lukebemish.modularmetals.util.MoreCodecs
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import io.github.groovymc.cgl.api.transform.codec.WithCodec
import io.github.groovymc.cgl.reg.RegistryObject
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.food.FoodProperties
import net.minecraft.world.item.Item
import net.minecraft.world.item.Rarity

@CodecSerializable(property = 'ITEM_CODEC')
@TupleConstructor(includeSuperProperties = true, callSuper = true, includeFields = true)
@CompileStatic
class ItemVariant extends Variant {
    protected ItemVariantTexturing texturing
    final Either<String,Map<String,String>> name
    final Optional<Fillable<List<String>>> tags
    final Optional<Fillable<ItemPropertiesBuilder>> itemProperties
    final Optional<Fillable<CompoundTag>> defaultItemTag

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
            final boolean meat = false
            final boolean fast = false
            final boolean alwaysEat = false
            final List<MobEffectProvider> effects = []

            FoodProperties makeProperties() {
                var builder = new FoodProperties.Builder()
                    .nutrition(nutrition)
                    .saturationMod(saturation)
                if (meat) builder = builder.meat()
                if (fast) builder = builder.fast()
                if (alwaysEat) builder = builder.alwaysEat()
                return Services.PLATFORM.platformData(builder, effects)
            }
        }
    }

    @Override
    Codec getCodec() {
        return ITEM_CODEC
    }

    ItemVariantTexturing getTexturing() {
        return this.@texturing
    }

    @TupleConstructor
    @CodecSerializable(property = 'ITEM_CODEC')
    static class ItemVariantTexturing {
        final Optional<MapHolder> generator
        final Optional<Map<String,MapHolder>> generators
        final Optional<MapHolder> model
        final Optional<Map<String,MapHolder>> models
        final TexSourceMap template

        Optional<Map<String, MapHolder>> getSimplifiedGenerator() {
            if (!generator.isPresent() && !generators.isPresent()) {
                return Optional.empty()
            }
            Map<String, MapHolder> out = [:]
            if (generator.isPresent()) {
                out[''] = generator.get()
            }
            if (generators.isPresent()) {
                out.putAll(generators.get())
            }
            return Optional.of(out)
        }

        Optional<Map<String, MapHolder>> getSimplifiedModel() {
            if (!model.isPresent() && !models.isPresent()) {
                return Optional.empty()
            }
            Map<String, MapHolder> out = [:]
            if (model.isPresent()) {
                out[''] = model.get()
            }
            if (models.isPresent()) {
                out.putAll(models.get())
            }
            return Optional.of(out)
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
        return ModularMetalsCommon.ITEMS.register(location, {->new MMItem(makeProperties(props))})
    }

    MMItemProps makeProperties(Map props) {
        return new MMItemProps(
            itemProperties
                .flatMap {it.apply(props).result()}
                .map { it.makeProperties() }
                .orElse(new Item.Properties()),
            defaultItemTag
                .flatMap {it.apply(props).result()}
        )
    }

    List<ResourceLocation> getTags(ResourceLocation metalRl, Map props) {
        return (tags.flatMap {it.apply(props).result()}.orElse([])).collect {ResourceLocation.of(it.replaceAll(/%s/, metalRl.path), ':' as char)}
    }
}
