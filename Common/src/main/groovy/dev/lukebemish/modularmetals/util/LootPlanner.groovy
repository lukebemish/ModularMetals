package dev.lukebemish.modularmetals.util

import com.google.gson.JsonElement
import dev.lukebemish.modularmetals.data.recipe.LootRecipe
import groovy.transform.CompileStatic
import net.minecraft.resources.ResourceLocation

@CompileStatic
final class LootPlanner {
    private LootPlanner() {}

    static final Map<ResourceLocation, List<LootRecipe.BakedLootRecipe>> recipes = [:]

    static JsonElement modify(ResourceLocation location, JsonElement element) {
        if (recipes.containsKey(location)) {
            for (LootRecipe.BakedLootRecipe recipe : recipes[location]) {
                element = recipe.modify(element)
            }
        }
        element
    }
}
