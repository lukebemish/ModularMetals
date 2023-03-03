package io.github.lukebemish.modularmetals.data.recipe


import com.mojang.serialization.Codec
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import io.github.lukebemish.modularmetals.planner.RecipePlanner
import io.github.lukebemish.modularmetals.data.MapHolder
import io.github.lukebemish.modularmetals.data.Metal
import net.minecraft.resources.ResourceLocation

@CodecSerializable
@TupleConstructor(includeSuperProperties = true, callSuper = true)
class RecipeRecipe extends Recipe implements TemplateRecipe{

    final MapHolder template
    final List<ResourceLocation> requiredVariants

    @Override
    Codec getCodec() {
        return $CODEC
    }

    MapHolder provideTemplate() {
        return template
    }

    @Override
    List<ResourceLocation> provideRequiredVariants() {
        return requiredVariants
    }

    @Override
    void register(Metal metal, ResourceLocation metalLocation, ResourceLocation recipeLocation, Map<ResourceLocation, ResourceLocation> variantLocations) {
        var pair = this.init(template, metal, metalLocation, recipeLocation, variantLocations)
        if (pair == null) return
        RecipePlanner.instance.plan(pair.first, pair.second)
    }
}
