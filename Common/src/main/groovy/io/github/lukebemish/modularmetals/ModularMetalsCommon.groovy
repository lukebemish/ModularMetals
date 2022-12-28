package io.github.lukebemish.modularmetals

import com.google.common.base.Suppliers
import dev.lukebemish.dynamicassetgenerator.api.DataResourceCache
import dev.lukebemish.dynamicassetgenerator.api.ResourceCache
import groovy.transform.CompileStatic
import io.github.groovymc.cgl.reg.RegistrationProvider
import io.github.lukebemish.modularmetals.client.ModularMetalsClient
import io.github.lukebemish.modularmetals.data.Metal
import io.github.lukebemish.modularmetals.data.ModConfig
import io.github.lukebemish.modularmetals.data.variant.Variant
import io.github.lukebemish.modularmetals.services.IPlatformHelper
import io.github.lukebemish.modularmetals.services.Services
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block

import java.util.function.Supplier

@CompileStatic
final class ModularMetalsCommon {
    private static final Supplier<ModConfig> CONFIG = Suppliers.memoize({ModConfig.load()})

    static final RegistrationProvider<Item> ITEMS = RegistrationProvider.get(Registries.ITEM, Constants.MOD_ID)
    static final RegistrationProvider<Block> BLOCKS = RegistrationProvider.get(Registries.BLOCK, Constants.MOD_ID)

    private ModularMetalsCommon() {}

    static Set<ResourceLocation> enabledDefaultVariants = config.variants.findAll {it.value.defaultEnabled.orElse(true)}.collect {it.key}.toSet()
    static Set<ResourceLocation> enabledDefaultRecipes = config.recipes.findAll {it.value.defaultEnabled.orElse(true)}.collect {it.key}.toSet()

    public static final DataResourceCache DATA_CACHE = ResourceCache.register(new DataResourceCache(new ResourceLocation(Constants.MOD_ID, "data")))

    static void init() {
        register()
        if (Services.PLATFORM.isClient())
            ModularMetalsClient.init()

        DATA_CACHE.planSource(RecipePlanner.instance)
    }

    static ModConfig getConfig() {
        PsuedoRegisters.registerCodecs()
        return CONFIG.get()
    }

    static void register() {
        config.metals.each { metalRl, metal ->
            Set<ResourceLocation> variantRls = getVariants(metalRl)
            for (ResourceLocation variantRl : variantRls) {
                ResourceLocation fullLocation = assembleMetalVariantName(metalRl, variantRl)
                Variant variant = config.variants.get(variantRl)
                variant.register(metal, metalRl, variantRl)
            }
            Set<ResourceLocation> recipeRls = getRecipes(metalRl)
            for (ResourceLocation recipeRl : recipeRls) {
                config.recipes.get(recipeRl).register(metal, metalRl, recipeRl, variantRls)
            }
        }
    }

    static Set<ResourceLocation> getVariants(ResourceLocation metal) {
        Set<ResourceLocation> variants = new HashSet<>(enabledDefaultVariants)
        Metal m = config.metals.get(metal)
        variants.removeAll {m.disallowedVariants.isPresent() && m.disallowedVariants.get().matches(it)}
        variants.addAll(config.variants.keySet().findAll {m.allowedVariants.isPresent() && m.allowedVariants.get().matches(it)})

        return variants
    }

    static Set<ResourceLocation> getRecipes(ResourceLocation metal) {
        Set<ResourceLocation> recipes = new HashSet<>(enabledDefaultRecipes)
        Metal m = config.metals.get(metal)
        recipes.removeAll {m.disallowedRecipes.isPresent() && m.disallowedRecipes.get().matches(it)}
        recipes.addAll(config.recipes.keySet().findAll {m.allowedRecipes.isPresent() && m.allowedRecipes.get().matches(it)})

        return recipes
    }

    static ResourceLocation assembleMetalVariantName(ResourceLocation metal, ResourceLocation variant) {
        return new ResourceLocation(Constants.MOD_ID, "${metal.namespace}_${metal.path}_${variant.namespace}_${variant.path}")
    }

    static final Map sharedEnvMap = Collections.unmodifiableMap(['platform':switch (Services.PLATFORM.platform) {
        case IPlatformHelper.Platform.FORGE -> 'forge'
        case IPlatformHelper.Platform.QUILT -> 'quilt'
    }])
}
