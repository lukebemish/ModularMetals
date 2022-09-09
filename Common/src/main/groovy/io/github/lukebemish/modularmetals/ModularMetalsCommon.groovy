package io.github.lukebemish.modularmetals

import com.google.common.base.Suppliers
import groovy.transform.CompileStatic
import io.github.groovymc.cgl.reg.RegistrationProvider
import io.github.lukebemish.dynamic_asset_generator.api.DataResourceCache
import io.github.lukebemish.modularmetals.client.ModularMetalsClient
import io.github.lukebemish.modularmetals.data.Metal
import io.github.lukebemish.modularmetals.data.ModConfig
import io.github.lukebemish.modularmetals.data.variant.BlockVariant
import io.github.lukebemish.modularmetals.data.variant.ItemVariant
import io.github.lukebemish.modularmetals.data.variant.Variant
import io.github.lukebemish.modularmetals.services.IPlatformHelper
import io.github.lukebemish.modularmetals.services.Services
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block

import java.util.function.Supplier

@CompileStatic
final class ModularMetalsCommon {
    private static final Supplier<ModConfig> CONFIG = Suppliers.memoize({ModConfig.load()})

    static final RegistrationProvider<Item> ITEMS = RegistrationProvider.get(Registry.ITEM_REGISTRY, Constants.MOD_ID)
    static final RegistrationProvider<Block> BLOCKS = RegistrationProvider.get(Registry.BLOCK_REGISTRY, Constants.MOD_ID)

    private ModularMetalsCommon() {}

    static Set<ResourceLocation> enabledDefaultVariants = config.variants.findAll {it.value.enabledByDefault}.collect {it.key}.toSet()

    static void init() {
        register()
        if (Services.PLATFORM.isClient())
            ModularMetalsClient.init()

        DataResourceCache.INSTANCE.planSource(RecipePlanner.instance)
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
                if (variant instanceof ItemVariant) {
                    variant.registerItem(fullLocation.path, metal, metalRl)
                    if (variant instanceof BlockVariant) {
                        variant.registerBlock(fullLocation.path, metal, metalRl)
                    }
                }
            }
            config.recipes.each {recipeRl, recipe ->
                recipe.register(metal, metalRl, recipeRl, variantRls)
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

    static ResourceLocation assembleMetalVariantName(ResourceLocation metal, ResourceLocation variant) {
        return new ResourceLocation(Constants.MOD_ID, "${metal.namespace}_${metal.path}_${variant.namespace}_${variant.path}")
    }

    static final Map sharedEnvMap = Collections.unmodifiableMap(['platform':switch (Services.PLATFORM.platform) {
        case IPlatformHelper.Platform.FORGE -> 'forge'
        case IPlatformHelper.Platform.QUILT -> 'quilt'
    }])
}
