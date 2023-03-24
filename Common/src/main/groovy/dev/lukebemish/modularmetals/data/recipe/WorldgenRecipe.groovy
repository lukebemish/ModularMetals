package dev.lukebemish.modularmetals.data.recipe

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.mojang.serialization.Codec
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import dev.lukebemish.modularmetals.data.MapHolder
import dev.lukebemish.modularmetals.data.Metal
import dev.lukebemish.modularmetals.data.filter.Filter
import dev.lukebemish.modularmetals.util.DataPlanner
import dev.lukebemish.modularmetals.util.TemplateUtils
import net.minecraft.resources.ResourceLocation

@CodecSerializable
@TupleConstructor(includeSuperProperties = true, callSuper = true)
class WorldgenRecipe extends Recipe {
    final MapHolder placedFeatureTemplate
    final MapHolder configuredFeatureTemplate
    final List<ResourceLocation> requiredVariants
    final Filter biomeFilter

    @Override
    void register(Metal metal, ResourceLocation metalLocation, ResourceLocation recipeLocation, Map<ResourceLocation, ResourceLocation> variantLocations) {
        var configured = TemplateUtils.init(configuredFeatureTemplate, metal, metalLocation, recipeLocation, variantLocations, requiredVariants)
        var placed = TemplateUtils.init(placedFeatureTemplate, metal, metalLocation, recipeLocation, variantLocations, requiredVariants)
        if (configured == null || placed == null) return
        JsonElement placedFeature = placed.second
        if (placedFeature instanceof JsonObject) {
            placedFeature.add('feature', new JsonPrimitive(configured.first.toString()))
        }
        DataPlanner.instance.feature(configured.first, configured.second, placedFeature)
    }

    @Override
    Codec getCodec() {
        return $CODEC
    }
}
