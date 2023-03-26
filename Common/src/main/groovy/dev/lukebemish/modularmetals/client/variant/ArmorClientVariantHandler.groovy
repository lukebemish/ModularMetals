package dev.lukebemish.modularmetals.client.variant

import com.mojang.blaze3d.platform.NativeImage
import dev.lukebemish.dynamicassetgenerator.api.ResourceGenerationContext
import dev.lukebemish.modularmetals.client.planner.TexturePlanner
import dev.lukebemish.modularmetals.data.Metal
import dev.lukebemish.modularmetals.data.variant.ItemVariant
import dev.lukebemish.modularmetals.data.variant.Variant
import dev.lukebemish.modularmetals.data.variant.armor.ArmorVariant
import groovy.transform.CompileStatic
import net.minecraft.resources.ResourceLocation

import java.util.function.Function

@CompileStatic
class ArmorClientVariantHandler extends ItemClientVariantHandler {

    @Override
    boolean processSpecial(ResourceLocation metalRl, Variant variant, String name, Function<ResourceGenerationContext, NativeImage> imageSource) {
        if (variant !instanceof ArmorVariant) {
            return false
        }
        if (name == 'armor1' || name == 'armor2') {
            //TODO: do stuff
            boolean is1 = name == 'armor1'
            TexturePlanner.instance.planArmor(metalRl, imageSource, is1)
            return true
        }
        return super.processSpecial(metalRl, variant, name, imageSource)
    }

    @Override
    protected void fillPlanners(Map<String, Map> models, ResourceLocation fullLocation, Map replacements, Metal metal, ItemVariant variant, ResourceLocation metalRl, ResourceLocation variantRl) {
        if (variant !instanceof ArmorVariant) {
            return
        }

        super.fillPlanners(models, fullLocation, replacements, metal, variant, metalRl, variantRl)
    }
}
