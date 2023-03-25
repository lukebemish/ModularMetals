package dev.lukebemish.modularmetals.data.recipe

import com.mojang.serialization.Codec
import dev.lukebemish.modularmetals.data.filter.string.AllStringFilter
import dev.lukebemish.modularmetals.data.filter.string.StringFilter
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.ExposeCodec
import dev.lukebemish.modularmetals.PseudoRegisters
import dev.lukebemish.modularmetals.data.Metal
import dev.lukebemish.modularmetals.data.ModConfig
import dev.lukebemish.modularmetals.util.CodecAware
import net.minecraft.resources.ResourceLocation

@TupleConstructor
@CompileStatic
abstract class Recipe implements CodecAware {
    final StringFilter requiredMods = AllStringFilter.instance
    @ExposeCodec
    static final Codec<Recipe> CODEC = ModConfig.dispatchedToDefaultResources(PseudoRegisters.RECIPE_TYPES, 'recipe type','recipe_templates')

    abstract void register(Metal metal, ResourceLocation metalLocation, ResourceLocation recipeLocation, Map<ResourceLocation, ResourceLocation> variantLocations)
}
