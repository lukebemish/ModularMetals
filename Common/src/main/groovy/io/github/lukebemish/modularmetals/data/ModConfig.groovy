package io.github.lukebemish.modularmetals.data

import blue.endless.jankson.JsonElement
import blue.endless.jankson.JsonObject
import blue.endless.jankson.api.SyntaxError
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.Decoder
import dev.lukebemish.defaultresources.api.ResourceProvider
import io.github.groovymc.cgl.api.codec.JanksonOps
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
    HashBiMap<ResourceLocation, Category> categories = HashBiMap.create()
    HashBiMap<ResourceLocation, Map<ResourceLocation, TexSourceMap>> templateSets = HashBiMap.create()

    static final Codec<Map<ResourceLocation, TexSourceMap>> TEMPLATE_SET_CODEC = Codec.unboundedMap(ResourceLocation.CODEC, TexSourceMap.NONEMPTY_CODEC)

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
            config.loadCategories()

            return config
        } catch (IOException e) {
            throw new RuntimeException(e)
        }
    }

    private static Collection<ResourceLocation> processResources(Collection<ResourceLocation> resources) {
        return resources
            .collect {rl -> new ResourceLocation(rl.namespace, rl.path.substring(0, rl.path.lastIndexOf('.'))) }
            .unique()
    }

    private static boolean isResource(ResourceLocation rl) {
        return rl.path.endsWith('.json') || rl.path.endsWith('.json5')
    }

    private void loadCategories() {
        var rls = processResources(ResourceProvider.instance().getResources(Constants.MOD_ID, "categories", ModConfig::isResource))

        for (ResourceLocation rl : rls) {
            ResourceLocation newRl = new ResourceLocation(rl.namespace, rl.path.substring('categories/'.length()))
            ResourceLocation jsonRl = new ResourceLocation(rl.namespace, rl.path + '.json')
            ResourceLocation json5Rl = new ResourceLocation(rl.namespace, rl.path + '.json5')
            try (Stream<? extends InputStream> resources = ResourceProvider.instance().getResourceStreams(Constants.MOD_ID, [json5Rl, jsonRl])) {
                Category category = Category.EMPTY
                resources.each { InputStream stream ->
                    try {
                        JsonObject json = Constants.JANKSON.load(stream)
                        Category read = ((Decoder<Category>) Category.$CODEC).parse(JanksonOps.COMMENTED, json).getOrThrow(false, {})
                        category = read.merge(category)
                    } catch (RuntimeException | SyntaxError | IOException e) {
                        Constants.LOGGER.error("Issues loading resource: {}", rl, e)
                    }
                    categories.put(newRl, category)
                }
            }
        }
    }

    private void loadMetals() {
        loadGeneralType(Metal.$CODEC, 'metals', this.metals)
    }

    private void loadVariants() {
        loadGeneralType(Variant.CODEC, 'variants', this.variants)
    }

    private void loadRecipes() {
        loadGeneralType(Recipe.CODEC, 'recipes', this.recipes)
    }

    private static <T> void loadGeneralType(Codec<T> codec, String type, BiMap<ResourceLocation, T> destination) {
        Decoder<T> decoder = codec
        var rls = processResources(ResourceProvider.instance().getResources(Constants.MOD_ID, type, ModConfig::isResource))

        for (ResourceLocation rl : rls) {
            ResourceLocation newRl = new ResourceLocation(rl.namespace, rl.path.substring(type.length() + 1))
            ResourceLocation jsonRl = new ResourceLocation(rl.namespace, rl.path + '.json')
            ResourceLocation json5Rl = new ResourceLocation(rl.namespace, rl.path + '.json5')
            try (var resources = ResourceProvider.instance().getResourceStreams(Constants.MOD_ID, [json5Rl, jsonRl])) {
                Optional<? extends InputStream> optional = resources.findFirst()
                if (optional.isPresent()) {
                    try {
                        JsonObject json = Constants.JANKSON.load(optional.get())
                        T resource = decoder.parse(JanksonOps.COMMENTED, json).getOrThrow(false, {})
                        destination.put(newRl, resource)
                    } catch (RuntimeException | SyntaxError | IOException e) {
                        Constants.LOGGER.error("Issues loading ${type} resource: ${rl}", e)
                    }
                }
            }
        }
    }

    private void loadTemplateSets() {
        var rls = processResources(ResourceProvider.instance().getResources(Constants.MOD_ID, "template_sets", ModConfig::isResource))

        for (ResourceLocation rl : rls) {
            ResourceLocation newRl = new ResourceLocation(rl.namespace, rl.path.substring('template_sets/'.length()))
            ResourceLocation jsonRl = new ResourceLocation(rl.namespace, rl.path + '.json')
            ResourceLocation json5Rl = new ResourceLocation(rl.namespace, rl.path + '.json5')
            try (Stream<? extends InputStream> resources = ResourceProvider.instance().getResourceStreams(Constants.MOD_ID, [json5Rl, jsonRl])) {
                Map<ResourceLocation, TexSourceMap> set = [:]
                resources.each { InputStream stream ->
                    try {
                        JsonObject json = Constants.JANKSON.load(stream)
                        Map<ResourceLocation, TexSourceMap> resource = TEMPLATE_SET_CODEC.parse(JanksonOps.COMMENTED, json).getOrThrow(false, {})
                        set.each { location, map ->
                            var innerMap = set.computeIfAbsent(location, {new TexSourceMap([:])})
                            map.value.each {k, v ->
                                innerMap.value.putIfAbsent(k, v)
                            }
                        }
                        set.putAll(resource)
                    } catch (RuntimeException | SyntaxError | IOException e) {
                        Constants.LOGGER.error("Issues loading template set: {}", rl, e)
                    }
                    templateSets.put(newRl, set)
                }
            }
        }
    }

    static <O extends CodecAware<O>> Codec<O> dispatchedToDefaultResources(BiMap<ResourceLocation, Codec<? extends O>> lookup, String name, String dirName) {
        CodecMapCodec.<O,JsonElement>dispatchWithInherit(lookup, name,
                {
                    try (Stream<InputStream> streams = Stream.<InputStream>concat(ResourceProvider.instance().getResourceStreams(Constants.MOD_ID,
                            new ResourceLocation(it.namespace,"${dirName}/${it.path}.json5")),ResourceProvider.instance().getResourceStreams(Constants.MOD_ID,
                            new ResourceLocation(it.namespace,"${dirName}/${it.path}.json")))) {
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
