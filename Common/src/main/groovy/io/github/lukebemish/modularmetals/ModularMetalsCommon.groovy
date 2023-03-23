package io.github.lukebemish.modularmetals

import com.google.common.base.Suppliers
import dev.lukebemish.dynamicassetgenerator.api.DataResourceCache
import dev.lukebemish.dynamicassetgenerator.api.ResourceCache
import io.github.groovymc.cgl.reg.RegistrationProvider
import io.github.lukebemish.modularmetals.client.ModularMetalsClient
import io.github.lukebemish.modularmetals.data.Category
import io.github.lukebemish.modularmetals.data.Metal
import io.github.lukebemish.modularmetals.data.ModConfig
import io.github.lukebemish.modularmetals.data.variant.Variant
import io.github.lukebemish.modularmetals.util.DataPlanner
import io.github.lukebemish.modularmetals.services.IPlatformHelper
import io.github.lukebemish.modularmetals.services.Services
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block

import java.util.function.Supplier

final class ModularMetalsCommon {
    private static final Supplier<ModConfig> CONFIG = Suppliers.memoize({ModConfig.load()})

    static final RegistrationProvider<Item> ITEMS = RegistrationProvider.get(Registries.ITEM, Constants.MOD_ID)
    static final RegistrationProvider<Block> BLOCKS = RegistrationProvider.get(Registries.BLOCK, Constants.MOD_ID)

    private ModularMetalsCommon() {}

    public static final DataResourceCache DATA_CACHE = ResourceCache.register(new DataResourceCache(new ResourceLocation(Constants.MOD_ID, "data")))

    static void init() {
        register()
        if (Services.PLATFORM.isClient())
            ModularMetalsClient.init()

        DATA_CACHE.planSource(DataPlanner.instance)
    }

    static ModConfig getConfig() {
        PsuedoRegisters.registerCodecs()
        return CONFIG.get()
    }

    static void register() {
        config.metals.each { metalRl, metal ->
            var existingVariants = metal.existingVariants.orElse(Map.of())
            Set<ResourceLocation> variantRls = getVariants(metalRl)
            Map<ResourceLocation, ResourceLocation> variantLocations = new HashMap<>()
            for (ResourceLocation variantRl : variantRls) {
                variantLocations.put(variantRl, assembleMetalVariantName(metalRl, variantRl))
            }
            variantLocations.putAll(existingVariants)
            for (ResourceLocation variantRl : variantRls) {
                Variant variant = config.variants.get(variantRl)
                variant.register(metal, metalRl, variantRl, variantLocations)
            }
            Set<ResourceLocation> recipeRls = getRecipes(metalRl)
            for (ResourceLocation recipeRl : recipeRls) {
                config.recipes.get(recipeRl).register(metal, metalRl, recipeRl, variantLocations)
            }
        }
    }

    static Set<ResourceLocation> getVariants(ResourceLocation metal) {
        Set<ResourceLocation> variants = new HashSet<>()
        Metal m = config.metals.get(metal)
        m.categories.each {
            Category category = config.categories.getOrDefault(it, Category.EMPTY)
            variants.addAll(category.variants)
        }

        variants.removeAll(m.banVariants.orElse(List.of()))
        variants.removeAll(m.existingVariants.orElse(Map.of()).keySet())

        return variants
    }

    static Set<ResourceLocation> getRecipes(ResourceLocation metal) {
        Set<ResourceLocation> recipes = new HashSet<>()
        Metal m = config.metals.get(metal)
        m.categories.each {
            Category category = config.categories.getOrDefault(it, Category.EMPTY)
            recipes.addAll(category.recipes)
        }

        recipes.removeAll(m.banRecipes.orElse(List.of()))

        return recipes
    }

    static ResourceLocation assembleMetalVariantName(ResourceLocation metal, ResourceLocation variant) {
        return new ResourceLocation(Constants.MOD_ID, "${metal.namespace}__${metal.path}___${variant.namespace}__${variant.path}")
    }

    static final Map sharedEnvMap = Collections.unmodifiableMap(['platform':switch (Services.PLATFORM.platform) {
        case IPlatformHelper.Platform.FORGE -> 'forge'
        case IPlatformHelper.Platform.QUILT -> 'quilt'
    }])
}
