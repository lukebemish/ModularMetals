package io.github.lukebemish.modularmetals.data.recipe

import com.mojang.serialization.Codec
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import io.github.lukebemish.modularmetals.data.MapHolder
import io.github.lukebemish.modularmetals.data.Metal
import io.github.lukebemish.modularmetals.util.DataPlanner
import io.github.lukebemish.modularmetals.util.TemplateUtils
import net.minecraft.resources.ResourceLocation

@CodecSerializable
@TupleConstructor(includeSuperProperties = true, callSuper = true)
class RecipeRecipe extends Recipe {

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
    void register(Metal metal, ResourceLocation metalLocation, ResourceLocation recipeLocation, Map<ResourceLocation, ResourceLocation> variantLocations) {
        var pair = TemplateUtils.init(template, metal, metalLocation, recipeLocation, variantLocations, requiredVariants)
        if (pair == null) return
        DataPlanner.instance.recipe(pair.first, pair.second)
    }
}
