package io.github.lukebemish.modularmetals.data.recipe

import com.google.gson.JsonElement
import com.mojang.datafixers.util.Pair
import com.mojang.serialization.JsonOps
import groovy.transform.CompileStatic
import io.github.groovymc.cgl.api.codec.ObjectOps
import io.github.lukebemish.modularmetals.Constants
import io.github.lukebemish.modularmetals.ModularMetalsCommon
import io.github.lukebemish.modularmetals.data.MapHolder
import io.github.lukebemish.modularmetals.data.Metal
import io.github.lukebemish.modularmetals.util.MapUtil
import net.minecraft.resources.ResourceLocation
import org.apache.groovy.io.StringBuilderWriter

@CompileStatic
interface TemplateRecipe {
    List<ResourceLocation> provideRequiredVariants()

    default Pair<ResourceLocation, JsonElement> init(MapHolder template, Metal metal, ResourceLocation metalLocation, ResourceLocation recipeLocation, Map<ResourceLocation, ResourceLocation> variantLocations) {
        var requiredVariants = provideRequiredVariants()
        if (!variantLocations.keySet().containsAll(requiredVariants))
            return null
        Map map = template.map
        Map replacements = ['variants':requiredVariants.collectEntries {
            [it.toString(), variantLocations[it].toString()]
        },'metal':metalLocation]
        replacements += ModularMetalsCommon.sharedEnvMap
        Map out
        try {
            out = MapUtil.replaceInMap(map, {
                var writer = new StringBuilderWriter()
                Constants.ENGINE.createTemplate(it)
                    .make(replacements)
                    .writeTo(writer)
                return writer.builder.toString()
            })
        } catch (Exception e) {
            Constants.LOGGER.error("Error filling out templated string for recipe ${recipeLocation}, metal ${metalLocation}: ",e)
            return null
        }
        ResourceLocation outputLocation = new ResourceLocation(Constants.MOD_ID, "${metalLocation.namespace}_${metalLocation.path}_${recipeLocation.namespace}_${recipeLocation.path}")
        JsonElement json = ObjectOps.instance.convertTo(JsonOps.INSTANCE,out)
        return new Pair<>(outputLocation, json)
    }
}
