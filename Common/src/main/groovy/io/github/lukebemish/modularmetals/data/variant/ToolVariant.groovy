package io.github.lukebemish.modularmetals.data.variant

import com.google.common.base.Suppliers
import com.mojang.serialization.DataResult
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import io.github.lukebemish.groovyduvet.wrapper.minecraft.api.codec.ObjectOps
import io.github.lukebemish.modularmetals.Constants
import io.github.lukebemish.modularmetals.ModularMetalsCommon
import io.github.lukebemish.modularmetals.data.Metal
import io.github.lukebemish.modularmetals.data.tier.ModularTier
import io.github.lukebemish.modularmetals.services.Services
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.Tier
import net.minecraft.world.item.TieredItem
import net.minecraft.world.item.crafting.Ingredient

import java.util.function.Supplier

@CompileStatic
@InheritConstructors
abstract class ToolVariant extends ItemVariant {

    private static ModularTier getFailedTier() {
        return ModularTier.CODEC.parse(ObjectOps.instance, [:]).getOrThrow(false, {})
    }

    static ModularTier getTier(Metal metal, ResourceLocation location) {
        return ModularTier.getOrCreateTier(location, {->
            DataResult<ModularTier> result = metal.getPropertyFromMap(new ResourceLocation(Constants.MOD_ID, 'tier'))?.decode(ModularTier.CODEC)
            return (result?.result()?.orElseGet({->
                Constants.LOGGER.error("Issue loading tier information for metal ${location}: ${result.error().get().message()}")
                return getFailedTier()
            })?:getFailedTier()).bake(location)
        })
    }

    private static Supplier<Ingredient> defaultIngredient(ResourceLocation location) {
        return Suppliers.memoize {->
            Ingredient.of(TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(Constants.MOD_ID,"ingots/${location.path}")))
        }
    }

    @Override
    void registerItem(String location, ResourceLocation variantRl, ResourceLocation metalRl, Metal metal) {
        ModularMetalsCommon.ITEMS.register(location, {->
            return getToolItemSupplier().getItem(getTier(metal, metalRl), getAttackModifier(), getSpeedModifier(),
                    new Item.Properties().tab(Services.PLATFORM.getItemTab()))
        })
    }

    abstract float getAttackModifier()
    abstract float getSpeedModifier()
    abstract ToolItemSupplier getToolItemSupplier()

    interface ToolItemSupplier {
        TieredItem getItem(Tier tier, float attackModifier, float speedModifier, Item.Properties properties)
    }
}
