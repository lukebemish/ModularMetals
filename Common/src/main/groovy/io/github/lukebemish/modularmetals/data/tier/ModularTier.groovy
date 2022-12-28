package io.github.lukebemish.modularmetals.data.tier

import com.google.common.base.Suppliers
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import groovy.transform.CompileStatic
import groovy.transform.Memoized
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.ExposeCodec
import io.github.lukebemish.modularmetals.Constants
import io.github.lukebemish.modularmetals.data.UtilCodecs
import io.github.lukebemish.modularmetals.services.IPlatformHelper
import io.github.lukebemish.modularmetals.services.Services
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Tier
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.block.Block
import org.jetbrains.annotations.Nullable

import java.util.function.Supplier

@CompileStatic
@TupleConstructor(excludes = ['repairIngredientSupplier'])
class ModularTier implements Tier {
    @ExposeCodec
    static final Codec<ModularTier> CODEC = RecordCodecBuilder.<ModularTier>create(i -> i.group(
            Codec.INT.optionalFieldOf('uses',59).forGetter({ModularTier it -> it.uses}) as RecordCodecBuilder<ModularTier, Integer>,
            Codec.FLOAT.optionalFieldOf('speed',2.0f).forGetter({ModularTier it -> it.speed}) as RecordCodecBuilder<ModularTier, Float>,
            Codec.FLOAT.optionalFieldOf('attack_bonus',0f).forGetter({ModularTier it -> it.attackDamageBonus}) as RecordCodecBuilder<ModularTier, Float>,
            Codec.INT.optionalFieldOf('level',0).forGetter({ModularTier it -> it.level}) as RecordCodecBuilder<ModularTier, Integer>,
            Codec.INT.optionalFieldOf('enchantment',14).forGetter({ModularTier it -> it.enchantmentValue}) as RecordCodecBuilder<ModularTier, Integer>,
            UtilCodecs.INGREDIENT_CODEC.optionalFieldOf('repair_ingredient').forGetter({ModularTier it -> it.repairIngredientSupplierOptional}) as RecordCodecBuilder<ModularTier, Optional<Supplier<Ingredient>>>,
            ResourceLocation.CODEC.listOf().optionalFieldOf('after',List.of(new ResourceLocation("wood"))).forGetter({ModularTier it -> it.after}) as RecordCodecBuilder<ModularTier, List<ResourceLocation>>,
            ResourceLocation.CODEC.listOf().optionalFieldOf('before',[]).forGetter({ModularTier it -> it.before}) as RecordCodecBuilder<ModularTier, List<ResourceLocation>>
    ).apply(i, ModularTier.&new))

    final int uses
    final float speed
    final float attackDamageBonus
    final int level
    final int enchantmentValue
    Supplier<Ingredient> repairIngredientSupplier
    final Optional<Supplier<Ingredient>> repairIngredientSupplierOptional
    final List<ResourceLocation> after
    final List<ResourceLocation> before

    ResourceLocation metalLocation

    ModularTier bake(ResourceLocation location) {
        repairIngredientSupplier = repairIngredientSupplierOptional.orElse(Suppliers.memoize {->
            Ingredient.of(TagKey.create(Registries.ITEM, new ResourceLocation(Constants.MOD_ID,"ingots/${location.path}")))
        })
        return this
    }

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
            return TagKey.create(Registries.BLOCK, new ResourceLocation(Constants.MOD_ID, "needs_${metalLocation}_tool"))
        else
            return TagKey.create(Registries.BLOCK, new ResourceLocation('fabric',"needs_tool_level_${level}"))
    }

    static Map<ResourceLocation, ModularTier> getTiers() {
        return Collections.unmodifiableMap(tiers)
    }
}
