package dev.lukebemish.modularmetals.util

import com.google.gson.JsonElement
import com.mojang.datafixers.util.Pair
import com.mojang.serialization.JsonOps
import dev.lukebemish.modularmetals.Constants
import dev.lukebemish.modularmetals.ModularMetalsCommon
import dev.lukebemish.modularmetals.data.MapHolder
import dev.lukebemish.modularmetals.data.Metal
import dev.lukebemish.modularmetals.template.MapUtil
import io.github.groovymc.cgl.api.codec.ObjectOps
import net.minecraft.resources.ResourceLocation

final class TemplateUtils {
    private TemplateUtils() {}

    static Pair<ResourceLocation, JsonElement> init(MapHolder template, Map initial, Metal metal, ResourceLocation metalLocation, ResourceLocation recipeLocation, Map<ResourceLocation, ResourceLocation> variantLocations, List<ResourceLocation> requiredVariants) {
        if (!variantLocations.keySet().containsAll(requiredVariants))
            return null
        Map map = template.map
        Map replacements = makeMap(initial, requiredVariants, variantLocations, metalLocation, metal)
        Map out
        try {
            out = MapUtil.evaluateEntries(map, replacements)
        } catch (Exception e) {
            Constants.LOGGER.error("Error filling out templated string for recipe ${recipeLocation}, metal ${metalLocation}: ",e)
            return null
        }
        ResourceLocation outputLocation = new ResourceLocation(Constants.MOD_ID, "${metalLocation.namespace}_${metalLocation.path}_${recipeLocation.namespace}_${recipeLocation.path}")
        JsonElement json = ObjectOps.instance.convertTo(JsonOps.INSTANCE,out)
        return new Pair<>(outputLocation, json)
    }

    static Map makeMap(Map initial, List<ResourceLocation> requiredVariants, Map<ResourceLocation, ResourceLocation> variantLocations, ResourceLocation metalLocation, Metal metal) {
        Map replacements = initial + ['variants': requiredVariants.collectEntries {
            [it.toString(), variantLocations[it].toString()]
        }, 'metal' : metalLocation, 'properties': metal.properties.collectEntries { [it.key.toString(), it.value.obj] }]
        replacements += ModularMetalsCommon.sharedEnvMap
        return replacements
    }

    static Pair<ResourceLocation, JsonElement> init(MapHolder template, Metal metal, ResourceLocation metalLocation, ResourceLocation recipeLocation, Map<ResourceLocation, ResourceLocation> variantLocations, List<ResourceLocation> requiredVariants) {
        return init(template, [:], metal, metalLocation, recipeLocation, variantLocations, requiredVariants)
    }
}
