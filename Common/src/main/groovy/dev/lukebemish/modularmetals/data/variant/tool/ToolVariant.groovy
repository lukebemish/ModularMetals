package dev.lukebemish.modularmetals.data.variant.tool

import com.mojang.serialization.DataResult
import dev.lukebemish.modularmetals.Constants
import dev.lukebemish.modularmetals.ModularMetalsCommon
import dev.lukebemish.modularmetals.data.Fillable
import dev.lukebemish.modularmetals.data.Metal
import dev.lukebemish.modularmetals.data.tier.ModularTier
import dev.lukebemish.modularmetals.data.variant.ItemVariant
import dev.lukebemish.modularmetals.objects.MMItemProps
import groovy.transform.InheritConstructors
import groovy.transform.Memoized
import io.github.groovymc.cgl.api.codec.ObjectOps
import io.github.groovymc.cgl.reg.RegistryObject
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.Tier
import net.minecraft.world.item.TieredItem

@InheritConstructors
abstract class ToolVariant extends ItemVariant {

    private static ModularTier getFailedTier() {
        return ModularTier.CODEC.parse(ObjectOps.instance, [:]).getOrThrow(false, {})
    }

    @Memoized static ModularTier getTier(Metal metal, ResourceLocation location) {
        return ModularTier.getOrCreateTier(location, {->
            DataResult<ModularTier> result = metal.getPropertyFromMap(new ResourceLocation(Constants.MOD_ID, 'tier'))?.decode(ModularTier.CODEC)
            return (result?.result()?.orElseGet({->
                Constants.LOGGER.error("Issue loading tier information for metal ${location}: ${result.error().get().message()}")
                return getFailedTier()
            })?:getFailedTier()).bake(location)
        })
    }

    @Override
    RegistryObject<? extends Item> registerItem(String location, ResourceLocation variantRl, ResourceLocation metalRl, Metal metal, Map props) {
        float attackModifier = getAttackModifier().apply(props).getOrThrow(false, {
            Constants.LOGGER.error("Attack modifier could not be parsed in variant ${variantRl} for metal ${metalRl}")
        })
        float speedModifier = getSpeedModifier().apply(props).getOrThrow(false, {
            Constants.LOGGER.error("Speed modifier could not be parsed in variant ${variantRl} for metal ${metalRl}")
        })
        ModularMetalsCommon.ITEMS.register(location, {->
            return getToolItemSupplier().getItem(getTier(metal, metalRl), attackModifier, speedModifier,
                    makeProperties(props))
        })
    }

    abstract Fillable<Float> getAttackModifier()
    abstract Fillable<Float> getSpeedModifier()
    abstract ToolItemSupplier getToolItemSupplier()

    interface ToolItemSupplier {
        TieredItem getItem(Tier tier, float attackModifier, float speedModifier, MMItemProps properties)
    }
}
