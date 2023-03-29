package dev.lukebemish.modularmetals.data.recipe

import com.google.gson.JsonElement
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import dev.lukebemish.modularmetals.data.Fillable
import dev.lukebemish.modularmetals.data.MapHolder
import dev.lukebemish.modularmetals.data.Metal
import dev.lukebemish.modularmetals.util.LootPlanner
import dev.lukebemish.modularmetals.util.ObjPredicate
import dev.lukebemish.modularmetals.util.TemplateUtils
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.codec.ObjectOps
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import net.minecraft.resources.ResourceLocation

@CodecSerializable
@CompileStatic
@TupleConstructor
class LootRecipe extends Recipe {
    List<ResourceLocation> injects
    ObjPredicate predicate
    Fillable<MapHolder> entry
    final List<ResourceLocation> requiredVariants

    @Override
    void register(Metal metal, ResourceLocation metalLocation, ResourceLocation recipeLocation, Map<ResourceLocation, ResourceLocation> variantLocations) {
        BakedLootRecipe baked = new BakedLootRecipe(
            TemplateUtils.makeMap([:], requiredVariants, variantLocations, metalLocation, metal),
            entry,
            predicate
        )
        injects.each {
            LootPlanner.recipes.computeIfAbsent(it, {[]}).add(baked)
        }
    }

    @Override
    Codec getCodec() {
        return $CODEC
    }

    @TupleConstructor
    static class BakedLootRecipe {
        Map props
        Fillable<MapHolder> entry
        ObjPredicate predicate

        JsonElement modify(JsonElement jsonElement) {
            Object obj = JsonOps.INSTANCE.convertTo(ObjectOps.instance, jsonElement)
            if (!obj instanceof Map) {
                return jsonElement
            }
            Map map = (Map) obj
            Object pools = map['pools']
            if (pools instanceof List) {
                pools.each {
                    if (it instanceof Map) {
                        Object entries = it['entries']
                        if (entries instanceof List) {
                            Object match = entries.find { e -> e instanceof Map && predicate.test(e) }
                            if (match !== null) {
                                Map templateProps = ['entry': match]
                                templateProps += this.props
                                var result = entry.apply(templateProps)
                                if (result.result().present) {
                                    entries.add(result.result().get().map)
                                } else {
                                    throw new RuntimeException("Could not modify loot table: ${result.error().get().message()}")
                                }
                            }
                        }
                    }
                }
            }
            return ObjectOps.instance.convertTo(JsonOps.INSTANCE, map)
        }
    }
}
