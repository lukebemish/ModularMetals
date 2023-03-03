package io.github.lukebemish.modularmetals.client

import com.google.common.base.Suppliers
import com.mojang.serialization.DataResult
import dev.lukebemish.dynamicassetgenerator.api.IPathAwareInputStreamSource
import dev.lukebemish.dynamicassetgenerator.api.ResourceCache
import dev.lukebemish.dynamicassetgenerator.api.ResourceGenerationContext
import dev.lukebemish.dynamicassetgenerator.api.client.AssetResourceCache
import dev.lukebemish.dynamicassetgenerator.api.client.generators.ITexSource
import dev.lukebemish.dynamicassetgenerator.api.client.generators.texsources.ErrorSource
import io.github.lukebemish.modularmetals.Constants
import io.github.lukebemish.modularmetals.ModularMetalsCommon
import io.github.lukebemish.modularmetals.client.planner.BlockstatePlanner
import io.github.lukebemish.modularmetals.client.planner.LangPlanner
import io.github.lukebemish.modularmetals.client.planner.ModelPlanner
import io.github.lukebemish.modularmetals.client.planner.TexturePlanner
import io.github.lukebemish.modularmetals.data.texsources.*
import io.github.lukebemish.modularmetals.data.variant.Variant
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.IoSupplier

import java.util.function.Supplier

class ModularMetalsClient {
    private ModularMetalsClient() {}

    public static final AssetResourceCache ASSET_CACHE = ResourceCache.register(new AssetResourceCache(new ResourceLocation(Constants.MOD_ID, "assets")))

    static void init() {
        ITexSource.register(new ResourceLocation(Constants.MOD_ID, "template"), VariantTemplateSource.$CODEC)
        ITexSource.register(new ResourceLocation(Constants.MOD_ID, "resolved"), ResolvedVariantSource.RESOLVED_CODEC)
        ITexSource.register(new ResourceLocation(Constants.MOD_ID, "easy_recolor"), EasyRecolorSource.$CODEC)
        ITexSource.register(new ResourceLocation(Constants.MOD_ID, "property_or_default"), PropertyOrDefaultSource.$CODEC)
        ITexSource.register(new ResourceLocation(Constants.MOD_ID, "with_template"), WithTemplateSource.$CODEC)

        ASSET_CACHE.planSource(TexturePlanner.instance)
        ASSET_CACHE.planSource(ModelPlanner.instance)
        ASSET_CACHE.planSource(BlockstatePlanner.instance)

        ASSET_CACHE.planSource(new IPathAwareInputStreamSource() {
            @Override
            Set<ResourceLocation> getLocations() {
                return Set.copyOf(LangPlanner.instance.languages().collect({new ResourceLocation(Constants.MOD_ID, "lang/${it}.json")}))
            }

            @Override
            IoSupplier<InputStream> get(ResourceLocation outRl, ResourceGenerationContext context) {
                return LangPlanner.instance.build(outRl.getPath().replace("lang/", "").replace(".json", ""))
            }
        })

        registerPlanners()
    }

    protected static void registerPlanners() {
        ModularMetalsCommon.config.metals.each {metalRl, metal ->
            Supplier<ITexSource> metalTexSource = Suppliers.memoize {->
                DataResult<ITexSource> result = metal.texturing.generator.decode(ITexSource.CODEC)
                return result.result().orElseGet({->new ErrorSource("Could not load texturing for metal ${metalRl}: ${result.error().get().message()}")})
            }
            Set<ResourceLocation> variantRls = ModularMetalsCommon.getVariants(metalRl)
            for (final variantRl : variantRls) {
                Variant variant = ModularMetalsCommon.config.variants.get(variantRl)
                variant.clientHandler.registerPlanners(metalRl, variantRl, metalTexSource, variant, metal)
            }
        }
    }
}
