package io.github.lukebemish.modularmetals.data.recipe

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.mojang.serialization.Codec
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import io.github.lukebemish.modularmetals.data.MapHolder
import io.github.lukebemish.modularmetals.data.Metal
import io.github.lukebemish.modularmetals.data.filter.Filter
import io.github.lukebemish.modularmetals.planner.WorldgenPlanner
import net.minecraft.resources.ResourceLocation

@CodecSerializable
@TupleConstructor(includeSuperProperties = true, callSuper = true)
class WorldgenRecipe extends Recipe implements TemplateRecipe {
    final MapHolder placedFeatureTemplate
    final MapHolder configuredFeatureTemplate
    final List<ResourceLocation> requiredVariants
    final Filter biomeFilter

    @Override
    void register(Metal metal, ResourceLocation metalLocation, ResourceLocation recipeLocation, Map<ResourceLocation, ResourceLocation> variantLocations) {
        var configured = this.init(configuredFeatureTemplate, metal, metalLocation, recipeLocation, variantLocations)
        var placed = this.init(placedFeatureTemplate, metal, metalLocation, recipeLocation, variantLocations)
        if (configured == null || placed == null) return
        JsonElement placedFeature = placed.second
        if (placedFeature instanceof JsonObject) {
            placedFeature.add('feature', new JsonPrimitive(configured.first.toString()))
        }
        WorldgenPlanner.instance.plan(configured.first, configured.second, placedFeature)
    }

    @Override
    List<ResourceLocation> provideRequiredVariants() {
        return requiredVariants
    }

    @Override
    Codec getCodec() {
        return $CODEC
    }
}
