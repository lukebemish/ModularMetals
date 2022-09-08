package io.github.lukebemish.modularmetals.data.variant

import com.google.common.base.Suppliers
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import io.github.groovymc.cgl.reg.RegistryObject
import io.github.lukebemish.modularmetals.Constants
import io.github.lukebemish.modularmetals.ModularMetalsCommon
import io.github.lukebemish.modularmetals.data.Metal
import io.github.lukebemish.modularmetals.data.UtilCodecs
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

    static ModularTier getTier(Metal metal, ResourceLocation location) {
        return ModularTier.getOrCreateTier(location, {->
            int uses = metal.getPropertyFromMap(new ResourceLocation(Constants.MOD_ID,"uses"))?.decode(CODEC.INT)?.result()?.orElse(59)?:59
            float speed = metal.getPropertyFromMap(new ResourceLocation(Constants.MOD_ID,"speed"))?.decode(CODEC.FLOAT)?.result()?.orElse(2.0f)?:2.0f
            float attackDamageBonus = metal.getPropertyFromMap(new ResourceLocation(Constants.MOD_ID,"attack_damage_bonus"))?.decode(CODEC.FLOAT)?.result()?.orElse(0f)?:0f
            int level = metal.getPropertyFromMap(new ResourceLocation(Constants.MOD_ID,"level"))?.decode(CODEC.INT)?.result()?.orElse(0)?:0
            int enchantmentValue = metal.getPropertyFromMap(new ResourceLocation(Constants.MOD_ID,"enchantment_value"))?.decode(CODEC.INT)?.result()?.orElse(15)?:15
            Supplier<Ingredient> repairIngredient = metal.getPropertyFromMap(new ResourceLocation(Constants.MOD_ID,"repair_ingredient"))?.decode(UtilCodecs.INGREDIENT_CODEC)?.result()?.orElse(defaultIngredient(location))?:defaultIngredient(location)
            List<ResourceLocation> after = metal.getPropertyFromMap(new ResourceLocation(Constants.MOD_ID,"after_tiers"))?.decode(UtilCodecs.singleOrList(ResourceLocation.CODEC))?.result()?.orElse(List.of(new ResourceLocation("wood")))?:List.of(new ResourceLocation("wood"))
            Optional<List<ResourceLocation>> before = Optional.ofNullable(metal.getPropertyFromMap(new ResourceLocation(Constants.MOD_ID,"before_tiers"))?.decode(UtilCodecs.singleOrList(ResourceLocation.CODEC))?.result()?.orElse(null))
            return new ModularTier(uses, speed, attackDamageBonus, level, enchantmentValue, repairIngredient, after, before, location)
        })
    }

    private static Supplier<Ingredient> defaultIngredient(ResourceLocation location) {
        return Suppliers.memoize {->
            Ingredient.of(TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(Constants.MOD_ID,"ingots/${location.path}")))
        }
    }

    @Override
    RegistryObject<? extends Item> registerItem(String location, Metal metal, ResourceLocation metalRl) {
        return ModularMetalsCommon.ITEMS.register(location, {->
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
