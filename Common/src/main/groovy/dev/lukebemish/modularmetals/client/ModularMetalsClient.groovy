package dev.lukebemish.modularmetals.client

import com.google.common.base.Suppliers
import com.mojang.serialization.DataResult
import dev.lukebemish.dynamicassetgenerator.api.IPathAwareInputStreamSource
import dev.lukebemish.dynamicassetgenerator.api.ResourceCache
import dev.lukebemish.dynamicassetgenerator.api.ResourceGenerationContext
import dev.lukebemish.dynamicassetgenerator.api.client.AssetResourceCache
import dev.lukebemish.dynamicassetgenerator.api.client.generators.ITexSource
import dev.lukebemish.dynamicassetgenerator.api.client.generators.texsources.ErrorSource
import dev.lukebemish.modularmetals.Constants
import dev.lukebemish.modularmetals.ModularMetalsCommon
import dev.lukebemish.modularmetals.client.planner.BlockstatePlanner
import dev.lukebemish.modularmetals.client.planner.LangPlanner
import dev.lukebemish.modularmetals.client.planner.ModelPlanner
import dev.lukebemish.modularmetals.client.planner.TexturePlanner
import dev.lukebemish.modularmetals.data.texsources.*
import dev.lukebemish.modularmetals.data.variant.Variant
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
        ITexSource.register(new ResourceLocation(Constants.MOD_ID, "property_capture"), PropertyCaptureSource.CODEC)
        ITexSource.register(new ResourceLocation(Constants.MOD_ID, "property_check"), PropertyCheckSource.$CODEC)
        ITexSource.register(new ResourceLocation(Constants.MOD_ID, "with_template"), WithTemplateSource.$CODEC)
        ITexSource.register(new ResourceLocation(Constants.MOD_ID, "cacheless"), CachelessSource.CODEC)

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
