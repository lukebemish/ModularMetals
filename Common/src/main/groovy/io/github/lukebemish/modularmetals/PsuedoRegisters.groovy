package io.github.lukebemish.modularmetals

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.mojang.serialization.Codec
import groovy.transform.Memoized
import groovy.transform.PackageScope
import io.github.lukebemish.modularmetals.data.filter.*
import io.github.lukebemish.modularmetals.data.recipe.Recipe
import io.github.lukebemish.modularmetals.data.recipe.RecipeRecipe
import io.github.lukebemish.modularmetals.data.recipe.WorldgenRecipe
import io.github.lukebemish.modularmetals.data.variant.AxeVariant
import io.github.lukebemish.modularmetals.data.variant.BlockVariant
import io.github.lukebemish.modularmetals.data.variant.HoeVariant
import io.github.lukebemish.modularmetals.data.variant.ItemVariant
import io.github.lukebemish.modularmetals.data.variant.PickaxeVariant
import io.github.lukebemish.modularmetals.data.variant.ShovelVariant
import io.github.lukebemish.modularmetals.data.variant.SwordVariant
import io.github.lukebemish.modularmetals.data.variant.Variant
import net.minecraft.resources.ResourceLocation

final class PsuedoRegisters {
    private PsuedoRegisters() {}

    static final BiMap<ResourceLocation, Codec<? extends Variant>> VARIANT_TYPES = HashBiMap.create()
    static final BiMap<ResourceLocation, Codec<? extends Filter>> FILTER_TYPES = HashBiMap.create()
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

        FILTER_TYPES.put(new ResourceLocation(Constants.MOD_ID, "and"), AndFilter.$CODEC)
        FILTER_TYPES.put(new ResourceLocation(Constants.MOD_ID, "or"), OrFilter.$CODEC)
        FILTER_TYPES.put(new ResourceLocation(Constants.MOD_ID, "not"), NotFilter.$CODEC)
        FILTER_TYPES.put(new ResourceLocation(Constants.MOD_ID, "is"), IsFilter.$CODEC)
        FILTER_TYPES.put(new ResourceLocation(Constants.MOD_ID, "all"), AllFilter.ALL_CODEC)

        RECIPE_TYPES.put(new ResourceLocation(Constants.MOD_ID, "recipe"), RecipeRecipe.$CODEC)
        RECIPE_TYPES.put(new ResourceLocation(Constants.MOD_ID, "feature"), WorldgenRecipe.$CODEC)

        return null
    }
}
