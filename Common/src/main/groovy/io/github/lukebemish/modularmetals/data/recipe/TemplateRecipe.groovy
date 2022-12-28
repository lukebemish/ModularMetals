package io.github.lukebemish.modularmetals.data.recipe

import com.google.gson.JsonElement
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import groovy.text.SimpleTemplateEngine
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.codec.ObjectOps
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import io.github.lukebemish.modularmetals.Constants
import io.github.lukebemish.modularmetals.ModularMetalsCommon
import io.github.lukebemish.modularmetals.RecipePlanner
import io.github.lukebemish.modularmetals.data.MapHolder
import io.github.lukebemish.modularmetals.data.Metal
import io.github.lukebemish.modularmetals.services.Services
import io.github.lukebemish.modularmetals.util.MapUtil
import net.minecraft.resources.ResourceLocation
import org.apache.groovy.io.StringBuilderWriter
import org.codehaus.groovy.control.CompilerConfiguration

@CodecSerializable
@TupleConstructor(includeSuperProperties = true, callSuper = true)
class TemplateRecipe extends Recipe {
    private static final SimpleTemplateEngine ENGINE = new SimpleTemplateEngine(new GroovyShell(TemplateRecipe.classLoader,new CompilerConfiguration()
            .addCompilationCustomizers(Constants.MAP_ACCESS_IMPORT_CUSTOMIZER, Constants.MAP_ACCESS_AST_CUSTOMIZER)))

    final MapHolder template
    final List<ResourceLocation> requiredVariants
    final Optional<List<String>> requiredMods

    @Override
    Codec getCodec() {
        return $CODEC
    }

    @Override
    void register(Metal metal, ResourceLocation metalLocation, ResourceLocation recipeLocation, Set<ResourceLocation> variantLocations) {
        if (!requiredMods.orElse([]).every { Services.PLATFORM.isModPresent(it)})
            return
        if (!variantLocations.containsAll(requiredVariants))
            return
        Map map = this.template.map
        Map replacements = ['variants':requiredVariants.collectEntries {
            [it.toString(), ModularMetalsCommon.assembleMetalVariantName(metalLocation, it)]
        },'metal':metalLocation]
        replacements += ModularMetalsCommon.sharedEnvMap
        Map out
        try {
            out = MapUtil.replaceInMap(map, {
                var writer = new StringBuilderWriter()
                ENGINE.createTemplate(it)
                        .make(replacements)
                        .writeTo(writer)
                return writer.builder.toString()
            })
        } catch (Exception e) {
            Constants.LOGGER.error("Error filling out templated string for recipe ${recipeLocation}, metal ${metalLocation}: ",e)
            return
        }
        ResourceLocation outputLocation = new ResourceLocation(Constants.MOD_ID, "${metalLocation.namespace}_${metalLocation.path}_${recipeLocation.namespace}_${recipeLocation.path}")
        JsonElement json = ObjectOps.instance.convertTo(JsonOps.INSTANCE,out)
        RecipePlanner.instance.plan(outputLocation, json)
    }
}
