package io.github.lukebemish.modularmetals.data

import blue.endless.jankson.JsonElement
import blue.endless.jankson.JsonObject
import blue.endless.jankson.api.SyntaxError
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.Decoder
import groovy.transform.CompileStatic
import io.github.groovymc.cgl.api.codec.JanksonOps
import dev.lukebemish.defaultresources.api.ResourceProvider
import io.github.groovymc.cgl.api.transform.codec.CodecRetriever
import io.github.lukebemish.modularmetals.Constants
import io.github.lukebemish.modularmetals.data.recipe.Recipe
import io.github.lukebemish.modularmetals.data.variant.Variant
import io.github.lukebemish.modularmetals.util.CodecAware
import io.github.lukebemish.modularmetals.util.CodecMapCodec
import net.minecraft.resources.ResourceLocation

import java.util.stream.Stream

class ModConfig {

    HashBiMap<ResourceLocation, Metal> metals = HashBiMap.create()
    HashBiMap<ResourceLocation, Variant> variants = HashBiMap.create()
    HashBiMap<ResourceLocation, Recipe> recipes = HashBiMap.create()
    HashBiMap<ResourceLocation, Map<ResourceLocation, Map<String,Either<ResourceLocation,MapHolder>>>> templateSets = HashBiMap.create()

    static final Codec<Map<ResourceLocation, Map<String,Either<ResourceLocation,MapHolder>>>> TEMPLATE_SET_CODEC = Codec.<ResourceLocation, Map<String,Either<ResourceLocation,MapHolder>>>unboundedMap(ResourceLocation.CODEC,
            Codec.<ResourceLocation, Map<String,Either<ResourceLocation,MapHolder>>>either(ResourceLocation.CODEC,Codec.<String, Either<ResourceLocation,MapHolder>>unboundedMap(Codec.STRING, Codec.either(ResourceLocation.CODEC, MapHolder.CODEC))).<Map<String, ResourceLocation>>xmap({
                return it.map({
                    return ['':it]
                },{
                    return it
                })
            },{
                return Either.<ResourceLocation, Map<String,ResourceLocation>>right(it)
            }))

    static ModConfig getDefaultConfig() {
        return new ModConfig()
    }

    static ModConfig load() {
        try {
            ModConfig config = new ModConfig()

            config.loadTemplateSets()
            config.loadMetals()
            config.loadVariants()
            config.loadRecipes()

            return config
        } catch (IOException e) {
            throw new RuntimeException(e)
        }
    }

    private void loadMetals() {
        var rls = ResourceProvider.instance().getResources(Constants.MOD_ID, "metals", rl -> true)

        for (ResourceLocation rl : rls) {
            try (var resources = ResourceProvider.instance().getResourceStreams(Constants.MOD_ID, rl)) {
                Optional<? extends InputStream> optional = resources.findFirst()
                if (optional.isPresent()) {
                    try {
                        if (rl.path.endsWith(".json") || rl.path.endsWith(".json5")) {
                            ResourceLocation newRl = new ResourceLocation(rl.namespace, rl.path.substring('metals/'.length(),rl.path.lastIndexOf('.')))
                            JsonObject json = Constants.JANKSON.load(optional.get())
                            Metal resource = ((Decoder<Metal>) Metal.$CODEC).parse(JanksonOps.COMMENTED, json).getOrThrow(false, {})
                            this.metals.put(newRl, resource)
                        }
                    } catch (RuntimeException | SyntaxError | IOException e) {
                        Constants.LOGGER.error("Issues loading resource: {}", rl, e)
                    }
                }
            }
        }
    }

    private void loadVariants() {
        var rls = ResourceProvider.instance().getResources(Constants.MOD_ID, "variants", rl -> true)

        for (ResourceLocation rl : rls) {
            try (var resources = ResourceProvider.instance().getResourceStreams(Constants.MOD_ID, rl)) {
                Optional<? extends InputStream> optional = resources.findFirst()
                if (optional.isPresent()) {
                    try {
                        if (rl.path.endsWith(".json") || rl.path.endsWith(".json5")) {
                            ResourceLocation newRl = new ResourceLocation(rl.namespace, rl.path.substring('variants/'.length(),rl.path.lastIndexOf('.')))
                            JsonObject json = Constants.JANKSON.load(optional.get())
                            Variant resource = ((Decoder<Variant>)Variant.CODEC).parse(JanksonOps.COMMENTED, json).getOrThrow(false, {})
                            this.variants.put(newRl, resource)
                        }
                    } catch (RuntimeException | SyntaxError | IOException e) {
                        Constants.LOGGER.error("Issues loading resource: {}", rl, e)
                    }
                }
            }
        }
    }

    private void loadRecipes() {
        var rls = ResourceProvider.instance().getResources(Constants.MOD_ID, "recipes", rl -> true)

        for (ResourceLocation rl : rls) {
            try (var resources = ResourceProvider.instance().getResourceStreams(Constants.MOD_ID, rl)) {
                Optional<? extends InputStream> optional = resources.findFirst()
                if (optional.isPresent()) {
                    try {
                        if (rl.path.endsWith(".json") || rl.path.endsWith(".json5")) {
                            ResourceLocation newRl = new ResourceLocation(rl.namespace, rl.path.substring('recipes/'.length(),rl.path.lastIndexOf('.')))
                            JsonObject json = Constants.JANKSON.load(optional.get())
                            Recipe resource = ((Decoder<Recipe>)Recipe.CODEC).parse(JanksonOps.COMMENTED, json).getOrThrow(false, {})
                            this.recipes.put(newRl, resource)
                        }
                    } catch (RuntimeException | SyntaxError | IOException e) {
                        Constants.LOGGER.error("Issues loading resource: {}", rl, e)
                    }
                }
            }
        }
    }

    private void loadTemplateSets() {
        var rls = ResourceProvider.instance().getResources(Constants.MOD_ID, "template_sets", rl -> true)

        for (ResourceLocation rl : rls) {
            try (var resources = ResourceProvider.instance().getResourceStreams(Constants.MOD_ID, rl)) {
                Optional<? extends InputStream> optional = resources.findFirst()
                if (optional.isPresent()) {
                    try {
                        if (rl.path.endsWith(".json") || rl.path.endsWith(".json5")) {
                            ResourceLocation newRl = new ResourceLocation(rl.namespace, rl.path.substring('template_sets/'.length(),rl.path.lastIndexOf('.')))
                            JsonObject json = Constants.JANKSON.load(optional.get())
                            Map<ResourceLocation, Map<String,Either<ResourceLocation,MapHolder>>> resource = TEMPLATE_SET_CODEC.parse(JanksonOps.COMMENTED, json).getOrThrow(false, {})
                            this.templateSets.put(newRl, resource)
                        }
                    } catch (RuntimeException | SyntaxError | IOException e) {
                        Constants.LOGGER.error("Issues loading template set: {}", rl, e)
                    }
                }
            }
        }
    }

    static <O extends CodecAware<O>> Codec<O> dispatchedToDefaultResources(BiMap<ResourceLocation, Codec<? extends O>> lookup, String name, String dirName) {
        CodecMapCodec.<O,JsonElement>dispatchWithInherit(lookup, name,
                {
                    try (Stream<InputStream> streams = Stream.<InputStream>concat(ResourceProvider.instance().getResourceStreams(Constants.MOD_ID,
                            new ResourceLocation(it.namespace,"${dirName}/${it.path}.json")),ResourceProvider.instance().getResourceStreams(Constants.MOD_ID,
                            new ResourceLocation(it.namespace,"${dirName}/${it.path}.json5")))) {
                        Optional<? extends InputStream> optional = streams.findFirst()
                        if (optional.present) {
                            JsonObject json = Constants.JANKSON.load(optional.get())
                            return DataResult.<JsonElement>success(json)
                        }
                    } catch(Exception e) {
                        return DataResult.<JsonElement>error("Couldn't get inherited ${name} ${it}: ${e}")
                    }
                }, JanksonOps.COMMENTED)
    }
}
