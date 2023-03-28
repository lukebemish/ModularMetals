package dev.lukebemish.modularmetals.client.variant

import com.mojang.blaze3d.platform.NativeImage
import dev.lukebemish.dynamicassetgenerator.api.ResourceGenerationContext
import dev.lukebemish.modularmetals.client.planner.TexturePlanner
import dev.lukebemish.modularmetals.data.Metal
import dev.lukebemish.modularmetals.data.variant.ItemVariant
import dev.lukebemish.modularmetals.data.variant.Variant
import dev.lukebemish.modularmetals.data.variant.armor.HorseArmorVariant
import groovy.transform.CompileStatic
import net.minecraft.resources.ResourceLocation

import java.util.function.Function

@CompileStatic
class HorseArmorClientVariantHandler extends ItemClientVariantHandler {
    @Override
    boolean processSpecial(ResourceLocation fullLocation, ResourceLocation metalRl, Variant variant, String name, Function<ResourceGenerationContext, NativeImage> imageSource) {
        if (variant !instanceof HorseArmorVariant) {
            return false
        }
        if (name == 'armor') {
            ResourceLocation location = new ResourceLocation(
                "entity/horse/armor/horse_armor_${fullLocation.namespace}__${fullLocation.path}"
            )
            TexturePlanner.instance.plan(location, imageSource, List.of())
            return true
        }
        return super.processSpecial(fullLocation, metalRl, variant, name, imageSource)
    }

    @Override
    protected void fillPlanners(Map<String, Map> models, ResourceLocation fullLocation, Map replacements, Metal metal, ItemVariant variant, ResourceLocation metalRl, ResourceLocation variantRl) {
        if (variant !instanceof HorseArmorVariant) {
            return
        }

        super.fillPlanners(models, fullLocation, replacements, metal, variant, metalRl, variantRl)
    }
}
