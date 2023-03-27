package dev.lukebemish.modularmetals

import com.google.common.base.Suppliers
import dev.lukebemish.dynamicassetgenerator.api.DataResourceCache
import dev.lukebemish.dynamicassetgenerator.api.ResourceCache
import dev.lukebemish.modularmetals.util.MoreCodecs
import io.github.groovymc.cgl.reg.RegistrationProvider
import dev.lukebemish.modularmetals.client.ModularMetalsClient
import dev.lukebemish.modularmetals.data.Category
import dev.lukebemish.modularmetals.data.Metal
import dev.lukebemish.modularmetals.data.ModConfig
import dev.lukebemish.modularmetals.data.variant.Variant
import dev.lukebemish.modularmetals.util.DataPlanner
import dev.lukebemish.modularmetals.services.IPlatformHelper
import dev.lukebemish.modularmetals.services.Services
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
        if (Services.PLATFORM.isDevelopmentEnvironment()) {
            Constants.LOGGER.info '[DEV ONLY] Testing generated maps for vanilla codecs...'
            Constants.LOGGER.info "MatieralColor: ${MoreCodecs.MATERIAL_COLOR_MAP.keySet()}"
            Constants.LOGGER.info "Material: ${MoreCodecs.MATERIAL_MAP.keySet()}"
            Constants.LOGGER.info "PushReaction: ${MoreCodecs.PUSH_REACTION_MAP.keySet()}"
            Constants.LOGGER.info "SoundType: ${MoreCodecs.SOUND_TYPE_MAP.keySet()}"
            Constants.LOGGER.info "Rarity: ${MoreCodecs.RARITY_MAP.keySet()}"
            Constants.LOGGER.info "ArmorItem.Type: ${MoreCodecs.ARMOR_TYPE_MAP.keySet()}"
        }

        register()
        if (Services.PLATFORM.isClient())
            ModularMetalsClient.init()

        DATA_CACHE.planSource(DataPlanner.instance)
    }

    static ModConfig getConfig() {
        PseudoRegisters.registerCodecs()
        return CONFIG.get()
    }

    static void register() {
        config.metals.each { metalRl, metal ->
            var existingVariants = metal.existingVariants
            Set<ResourceLocation> variantRls = getVariants(metalRl)
            Map<ResourceLocation, ResourceLocation> variantLocations = new HashMap<>()
            for (ResourceLocation variantRl : variantRls) {
                variantLocations.put(variantRl, assembleMetalVariantName(metalRl, variantRl))
            }
            variantLocations.putAll(existingVariants)
            for (ResourceLocation variantRl : variantRls) {
                Variant variant = config.variants.get(variantRl)
                if (variant == null) {
                    throw new RuntimeException("Variant ${variantRl} not found!")
                }
                variant.register(metal, metalRl, variantRl, variantLocations)
            }
            Set<ResourceLocation> recipeRls = getRecipes(metalRl)
            for (ResourceLocation recipeRl : recipeRls) {
                var recipe = config.recipes.get(recipeRl)
                if (recipe == null) {
                    throw new RuntimeException("Recipe ${recipeRl} not found!")
                }
                recipe.register(metal, metalRl, recipeRl, variantLocations)
            }
        }
    }

    static Set<ResourceLocation> getVariants(ResourceLocation metal) {
        Set<ResourceLocation> variants = new HashSet<>()
        Metal m = config.metals.get(metal)
        m.categories.each {
            Category category = config.categories.getOrDefault(it, Category.EMPTY)
            variants.addAll(category.fullVariants)
        }

        variants.removeAll(m.banVariants)
        variants.removeAll(m.existingVariants.keySet())

        return variants
    }

    static Set<ResourceLocation> getRecipes(ResourceLocation metal) {
        Set<ResourceLocation> recipes = new HashSet<>()
        Metal m = config.metals.get(metal)
        m.categories.each {
            Category category = config.categories.getOrDefault(it, Category.EMPTY)
            recipes.addAll(category.fullRecipes)
        }

        recipes.removeAll(m.banRecipes)

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
