package dev.lukebemish.modularmetals.data.recipe

import com.mojang.serialization.Codec
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.ExposeCodec
import dev.lukebemish.modularmetals.PsuedoRegisters
import dev.lukebemish.modularmetals.data.Metal
import dev.lukebemish.modularmetals.data.ModConfig
import dev.lukebemish.modularmetals.util.CodecAware
import net.minecraft.resources.ResourceLocation

@TupleConstructor
abstract class Recipe implements CodecAware {
    final Optional<List<String>> requiredMods
    @ExposeCodec
    static final Codec<Recipe> CODEC = ModConfig.dispatchedToDefaultResources(PsuedoRegisters.RECIPE_TYPES, 'recipe type','recipes')

    abstract void register(Metal metal, ResourceLocation metalLocation, ResourceLocation recipeLocation, Map<ResourceLocation, ResourceLocation> variantLocations)
}
