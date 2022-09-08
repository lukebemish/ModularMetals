package io.github.lukebemish.modularmetals.data.tier

import groovy.transform.CompileStatic
import groovy.transform.Memoized
import groovy.transform.TupleConstructor
import io.github.lukebemish.modularmetals.Constants
import io.github.lukebemish.modularmetals.services.IPlatformHelper
import io.github.lukebemish.modularmetals.services.Services
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Tier
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.block.Block
import org.jetbrains.annotations.Nullable

import java.util.function.Supplier

@CompileStatic
@TupleConstructor
class ModularTier implements Tier {
    int uses
    float speed
    float attackDamageBonus
    int level
    int enchantmentValue
    Supplier<Ingredient> repairIngredientSupplier
    List<ResourceLocation> after
    Optional<List<ResourceLocation>> before

    ResourceLocation metalLocation

    private static final Map<ResourceLocation, ModularTier> tiers = [:]

    static ModularTier getOrCreateTier(ResourceLocation location, Supplier<ModularTier> supplier) {
        return tiers.computeIfAbsent(location, {supplier.get()})
    }

    @Nullable
    static ModularTier get(ResourceLocation location) {
        return tiers.get(location)
    }

    @Override
    Ingredient getRepairIngredient() {
        return repairIngredientSupplier.get()
    }

    // Used by Forge; added to appropriate dynamic tag on Quilt
    @Memoized TagKey<Block> getTag() {
        if (Services.PLATFORM.platform === IPlatformHelper.Platform.FORGE)
            return TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(Constants.MOD_ID, "needs_${metalLocation}_tool"))
        else
            return TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation('fabric',"needs_tool_level_${level}"))
    }

    static Map<ResourceLocation, ModularTier> getTiers() {
        return Collections.unmodifiableMap(tiers)
    }
}
