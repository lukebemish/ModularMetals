package dev.lukebemish.modularmetals.client.variant

import dev.lukebemish.dynamicassetgenerator.api.client.generators.ITexSource
import dev.lukebemish.modularmetals.data.Metal
import dev.lukebemish.modularmetals.data.variant.Variant
import net.minecraft.resources.ResourceLocation

import java.util.function.Supplier

interface ClientVariantHandler {
    void registerPlanners(ResourceLocation metalRl,
                          ResourceLocation variantRl,
                          Supplier<ITexSource> metalTexSource,
                          Variant variant,
                          Metal metal)
}
