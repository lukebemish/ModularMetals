package io.github.lukebemish.modularmetals.data.recipe

import com.mojang.serialization.Codec
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.ExposeCodec
import io.github.lukebemish.modularmetals.PsuedoRegisters
import io.github.lukebemish.modularmetals.data.Metal
import io.github.lukebemish.modularmetals.data.ModConfig
import io.github.lukebemish.modularmetals.util.CodecAware
import net.minecraft.resources.ResourceLocation

@TupleConstructor
abstract class Recipe implements CodecAware {
    final Optional<List<String>> requiredMods
    @ExposeCodec
    static final Codec<Recipe> CODEC = ModConfig.dispatchedToDefaultResources(PsuedoRegisters.RECIPE_TYPES, 'recipe type','recipes')

    abstract void register(Metal metal, ResourceLocation metalLocation, ResourceLocation recipeLocation, Map<ResourceLocation, ResourceLocation> variantLocations)
}
