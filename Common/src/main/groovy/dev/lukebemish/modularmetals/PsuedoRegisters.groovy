package dev.lukebemish.modularmetals

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.mojang.serialization.Codec
import dev.lukebemish.modularmetals.data.filter.resource.*
import dev.lukebemish.modularmetals.data.filter.string.*
import dev.lukebemish.modularmetals.data.recipe.Recipe
import dev.lukebemish.modularmetals.data.recipe.RecipeRecipe
import dev.lukebemish.modularmetals.data.recipe.WorldgenRecipe
import dev.lukebemish.modularmetals.data.variant.*
import groovy.transform.Memoized
import groovy.transform.PackageScope
import net.minecraft.resources.ResourceLocation

final class PsuedoRegisters {
    private PsuedoRegisters() {}

    static final BiMap<ResourceLocation, Codec<? extends Variant>> VARIANT_TYPES = HashBiMap.create()
    static final BiMap<ResourceLocation, Codec<? extends ResourceFilter>> RESOURCE_FILTER_TYPES = HashBiMap.create()
    static final BiMap<ResourceLocation, Codec<? extends StringFilter>> STRING_FILTER_TYPES = HashBiMap.create()
    static final BiMap<ResourceLocation, Codec<? extends Recipe>> RECIPE_TYPES = HashBiMap.create()

    @Memoized
    @PackageScope
    static Object registerCodecs() {
        VARIANT_TYPES.put(new ResourceLocation(Constants.MOD_ID, "block"), BlockVariant.$CODEC)
        VARIANT_TYPES.put(new ResourceLocation(Constants.MOD_ID, "item"), ItemVariant.$CODEC)
        VARIANT_TYPES.put(new ResourceLocation(Constants.MOD_ID, "shovel"), ShovelVariant.SHOVEL_CODEC)
        VARIANT_TYPES.put(new ResourceLocation(Constants.MOD_ID, "axe"), AxeVariant.AXE_CODEC)
        VARIANT_TYPES.put(new ResourceLocation(Constants.MOD_ID, "pickaxe"), PickaxeVariant.PICKAXE_CODEC)
        VARIANT_TYPES.put(new ResourceLocation(Constants.MOD_ID, "hoe"), HoeVariant.HOE_CODEC)
        VARIANT_TYPES.put(new ResourceLocation(Constants.MOD_ID, "sword"), SwordVariant.SWORD_CODEC)

        RESOURCE_FILTER_TYPES.put(new ResourceLocation(Constants.MOD_ID, "and"), AndResourceFilter.$CODEC)
        RESOURCE_FILTER_TYPES.put(new ResourceLocation(Constants.MOD_ID, "or"), OrResourceFilter.$CODEC)
        RESOURCE_FILTER_TYPES.put(new ResourceLocation(Constants.MOD_ID, "not"), NotResourceFilter.$CODEC)
        RESOURCE_FILTER_TYPES.put(new ResourceLocation(Constants.MOD_ID, "is"), IsResourceFilter.$CODEC)
        RESOURCE_FILTER_TYPES.put(new ResourceLocation(Constants.MOD_ID, "all"), AllResourceFilter.ALL_CODEC)
        RESOURCE_FILTER_TYPES.put(new ResourceLocation(Constants.MOD_ID, "tag"), TagResourceFilter.$CODEC)

        STRING_FILTER_TYPES.put(new ResourceLocation(Constants.MOD_ID, "and"), AndStringFilter.$CODEC)
        STRING_FILTER_TYPES.put(new ResourceLocation(Constants.MOD_ID, "or"), OrStringFilter.$CODEC)
        STRING_FILTER_TYPES.put(new ResourceLocation(Constants.MOD_ID, "not"), NotStringFilter.$CODEC)
        STRING_FILTER_TYPES.put(new ResourceLocation(Constants.MOD_ID, "is"), IsStringFilter.$CODEC)
        STRING_FILTER_TYPES.put(new ResourceLocation(Constants.MOD_ID, "all"), AllStringFilter.ALL_CODEC)

        RECIPE_TYPES.put(new ResourceLocation(Constants.MOD_ID, "recipe"), RecipeRecipe.$CODEC)
        RECIPE_TYPES.put(new ResourceLocation(Constants.MOD_ID, "feature"), WorldgenRecipe.$CODEC)

        return null
    }
}
