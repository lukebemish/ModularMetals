package io.github.lukebemish.modularmetals.data

import blue.endless.jankson.JsonObject
import blue.endless.jankson.api.SyntaxError
import com.google.common.collect.HashBiMap
import com.mojang.serialization.Decoder
import groovy.transform.CompileStatic
import io.github.groovymc.cgl.api.codec.JanksonOps
import io.github.lukebemish.defaultresources.api.ResourceProvider
import io.github.lukebemish.modularmetals.Constants
import io.github.lukebemish.modularmetals.data.variant.Variant
import net.minecraft.resources.ResourceLocation

@CompileStatic
class ModConfig {

    HashBiMap<ResourceLocation, Metal> metals = HashBiMap.create()
    HashBiMap<ResourceLocation, Variant> variants = HashBiMap.create()

    static ModConfig getDefaultConfig() {
        return new ModConfig()
    }

    static ModConfig load() {
        try {
            ModConfig config = new ModConfig()

            config.loadMetals()
            config.loadVariants()

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
                            Metal resource = ((Decoder<Metal>)Metal.$CODEC).parse(JanksonOps.COMMENTED, json).getOrThrow(false, {})
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
}
