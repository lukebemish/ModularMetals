package dev.lukebemish.modularmetals.data

import blue.endless.jankson.JsonElement
import blue.endless.jankson.JsonObject
import blue.endless.jankson.api.SyntaxError
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.Decoder
import dev.lukebemish.defaultresources.api.ResourceProvider
import dev.lukebemish.modularmetals.Constants
import dev.lukebemish.modularmetals.data.filter.resource.ResourceFilter
import dev.lukebemish.modularmetals.data.filter.resource.ResourceFilterFinder
import dev.lukebemish.modularmetals.data.filter.string.StringFilter
import dev.lukebemish.modularmetals.data.recipe.Recipe
import dev.lukebemish.modularmetals.data.variant.Variant
import dev.lukebemish.modularmetals.services.Services
import dev.lukebemish.modularmetals.util.CodecAware
import dev.lukebemish.modularmetals.util.CodecMapCodec

import io.github.groovymc.cgl.api.codec.JanksonOps
import net.minecraft.resources.ResourceLocation

import java.util.stream.Stream

class ModConfig {
    HashBiMap<ResourceLocation, Metal> metals = HashBiMap.create()
    HashBiMap<ResourceLocation, Variant> variants = HashBiMap.create()
    HashBiMap<ResourceLocation, Recipe> recipes = HashBiMap.create()
    HashBiMap<ResourceLocation, Category> categories = HashBiMap.create()
    HashBiMap<ResourceLocation, Map<ResourceLocation, TexSourceMap>> templateSets = HashBiMap.create()

    final ResourceFilterFinder<ResourceLocation> recipeFilterFinder = new ResourceFilterFinder<ResourceLocation>() {
        @Override
        boolean isTag(ResourceLocation thing, ResourceLocation tag) {
            return categories.get(tag)?.fullRecipes?.contains(thing)?:false
        }

        @Override
        boolean isLocation(ResourceLocation thing, ResourceLocation location) {
            return thing == location
        }
    }

    ResourceFilterFinder<ResourceLocation> variantFilterFinder = new ResourceFilterFinder<ResourceLocation>() {
        @Override
        boolean isTag(ResourceLocation thing, ResourceLocation tag) {
            return categories.get(tag)?.fullVariants?.contains(thing)?:false
        }

        @Override
        boolean isLocation(ResourceLocation thing, ResourceLocation location) {
            return thing == location
        }
    }

    static final Codec<Map<ResourceLocation, TexSourceMap>> TEMPLATE_SET_CODEC = Codec.unboundedMap(ResourceLocation.CODEC, TexSourceMap.NONEMPTY_CODEC)

    static ModConfig getDefaultConfig() {
        return new ModConfig()
    }

    static ModConfig load() {
        try {
            ModConfig config = new ModConfig()

            config.loadTemplateSets()
            config.loadMetals()
            config.loadCategories()
            config.loadMetalProperties()
            config.loadVariants()
            config.loadRecipes()

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
                }
                categories.put(newRl, category)
            }
        }
    }

    private void loadMetals() {
        loadGeneralType(Metal.$CODEC, 'metals', this.metals)
        Set<ResourceLocation> keys = new HashSet<>(this.metals.keySet())
        for (ResourceLocation rl : keys) {
            Metal metal = this.metals.get(rl)
            if (!metal.requiredMods.matches(Services.PLATFORM.modList(), StringFilter.SET_FINDER)) {
                this.metals.remove(rl)
            }
        }
    }

    private void loadMetalProperties() {
        var rls = processResources(ResourceProvider.instance().getResources(Constants.MOD_ID, "properties", ModConfig::isResource))

        for (ResourceLocation rl : rls) {
            ResourceLocation jsonRl = new ResourceLocation(rl.namespace, rl.path + '.json')
            ResourceLocation json5Rl = new ResourceLocation(rl.namespace, rl.path + '.json5')
            try (Stream<? extends InputStream> resources = ResourceProvider.instance().getResourceStreams(Constants.MOD_ID, [json5Rl, jsonRl])) {
                MetalProperties props = new MetalProperties()
                resources.each { InputStream stream ->
                    try {
                        JsonObject json = Constants.JANKSON.load(stream)
                        MetalProperties read = ((Decoder<MetalProperties>) MetalProperties.$CODEC).parse(JanksonOps.COMMENTED, json).getOrThrow(false, {})
                        props = props.mergeProperties(read)
                    } catch (RuntimeException | SyntaxError | IOException e) {
                        Constants.LOGGER.error("Issues loading resource: {}", rl, e)
                    }
                }
                metals.each { key,val ->
                    if (props.metals.matches(key, ResourceFilter.SIMPLE_FINDER)) {
                        val.mergeProperties(props)
                    }
                }
            }
        }
    }

    private void loadVariants() {
        loadGeneralType(Variant.CODEC, 'variants', this.variants)
        Set<ResourceLocation> keys = new HashSet<>(this.variants.keySet())
        for (ResourceLocation rl : keys) {
            Variant variant = this.variants.get(rl)
            if (!variant.requiredMods.matches(Services.PLATFORM.modList(), StringFilter.SET_FINDER)) {
                this.variants.remove(rl)
            }
        }
    }

    private void loadRecipes() {
        loadGeneralType(Recipe.CODEC, 'recipes', this.recipes)
        Set<ResourceLocation> keys = new HashSet<>(this.recipes.keySet())
        for (ResourceLocation rl : keys) {
            Recipe recipe = this.recipes.get(rl)
            if (!recipe.requiredMods.matches(Services.PLATFORM.modList(), StringFilter.SET_FINDER)) {
                this.recipes.remove(rl)
            }
        }
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
                        return DataResult.<JsonElement>error {->"Couldn't get inherited ${name} ${it}: ${e}"}
                    }
                }, JanksonOps.COMMENTED)
    }
}
