package io.github.lukebemish.modularmetals.forge

import com.matyrobbrt.gml.GMod
import com.mojang.serialization.Codec
import io.github.lukebemish.modularmetals.Constants
import io.github.lukebemish.modularmetals.ModularMetalsCommon
import io.github.lukebemish.modularmetals.data.tier.ModularTier
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraftforge.common.TierSortingRegistry
import net.minecraftforge.common.world.BiomeModifier
import net.minecraftforge.event.CreativeModeTabEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries

import java.util.function.Supplier

@GMod(Constants.MOD_ID)
class ModularMetalsForge {
    static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIER = DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, Constants.MOD_ID)

    static final List<Supplier<ItemStack>> TAB_ITEMS = new ArrayList<>()

    ModularMetalsForge() {
        ModularMetalsCommon.init()
        forgeBus.register(this)

        ModularTier.tiers.each {location, tier ->
            TierSortingRegistry.registerTier(tier, location,new ArrayList<Object>(tier.after),new ArrayList<Object>(tier.before))
        }

        BIOME_MODIFIER.register("filter_feature", {->FilterFeatureBiomeModifier.$CODEC} as Supplier<Codec<? extends BiomeModifier>>)

        modBus.register(this)
    }

    @SuppressWarnings('GrMethodMayBeStatic')
    @SubscribeEvent
    void onCreativeTabRegistry(CreativeModeTabEvent.Register event) {
        event.registerCreativeModeTab(new ResourceLocation(Constants.MOD_ID, "items"), {builder ->
            builder.icon {-> Items.IRON_INGOT.defaultInstance}
            builder.displayItems {flags, output, displayOp ->
                for (Supplier<ItemStack> item : TAB_ITEMS) {
                    output.accept(item.get())
                }
            }
        })
    }
}
