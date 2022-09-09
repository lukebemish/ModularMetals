package io.github.lukebemish.modularmetals.data.recipe

import com.mojang.serialization.Codec
import groovy.transform.CompileStatic
import io.github.lukebemish.groovyduvet.wrapper.minecraft.api.codec.ExposeCodec
import io.github.lukebemish.modularmetals.PsuedoRegisters
import io.github.lukebemish.modularmetals.data.Metal
import io.github.lukebemish.modularmetals.data.ModConfig
import io.github.lukebemish.modularmetals.util.CodecAware
import net.minecraft.resources.ResourceLocation

@CompileStatic
abstract class Recipe implements CodecAware {
    @ExposeCodec
    static final Codec<Recipe> CODEC = ModConfig.dispatchedToDefaultResources(PsuedoRegisters.RECIPE_TYPES, 'recipe type','recipes')

    abstract void register(Metal metal, ResourceLocation metalLocation, ResourceLocation recipeLocation, Set<ResourceLocation> variantLocations)
}
