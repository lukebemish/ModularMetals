package dev.lukebemish.modularmetals.util

import com.google.gson.JsonElement
import com.mojang.serialization.JsonOps
import dev.lukebemish.modularmetals.data.recipe.LootRecipe
import groovy.transform.CompileStatic
import io.github.groovymc.cgl.api.codec.ObjectOps
import net.minecraft.resources.ResourceLocation

@CompileStatic
final class LootPlanner {
    private LootPlanner() {}

    static final Map<ResourceLocation, List<LootRecipe.BakedLootRecipe>> recipes = [:]

    static JsonElement modify(ResourceLocation location, JsonElement element) {
        if (recipes.containsKey(location)) {
            Object obj = JsonOps.INSTANCE.convertTo(ObjectOps.instance, element)
            for (LootRecipe.BakedLootRecipe recipe : recipes[location]) {
                obj = recipe.modify(obj)
            }
            element = ObjectOps.instance.convertTo(JsonOps.INSTANCE, obj)
        }
        element
    }
}
