package io.github.lukebemish.modularmetals.forge

import com.matyrobbrt.gml.GMod
import groovy.transform.CompileStatic
import io.github.lukebemish.modularmetals.Constants
import io.github.lukebemish.modularmetals.ModularMetalsCommon
import io.github.lukebemish.modularmetals.data.tier.ModularTier
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraftforge.common.TierSortingRegistry

@GMod(Constants.MOD_ID)
@CompileStatic
class ModularMetalsForge {
    static final CreativeModeTab ITEM_TAB = new CreativeModeTab("${Constants.MOD_ID}.items") {
        @Override
        ItemStack makeIcon() {
            return new ItemStack(Items.IRON_INGOT)
        }
    }
    static final CreativeModeTab BLOCK_TAB = new CreativeModeTab("${Constants.MOD_ID}.blocks") {
        @Override
        ItemStack makeIcon() {
            return new ItemStack(Items.IRON_BLOCK)
        }
    }

    ModularMetalsForge() {
        ModularMetalsCommon.init()
        forgeBus.register(this)

        ModularTier.tiers.each {location, tier ->
            TierSortingRegistry.registerTier(tier, location,new ArrayList<Object>(tier.after),new ArrayList<Object>(tier.before.orElse([])))
        }
    }
}
