package io.github.lukebemish.modularmetals.quilt

import io.github.lukebemish.modularmetals.Constants
import io.github.lukebemish.modularmetals.ModularMetalsCommon
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer

import java.util.function.Supplier

class ModularMetalsQuilt implements ModInitializer {

    static final List<Supplier<ItemStack>> TAB_ITEMS = new ArrayList<>()

    static final CreativeModeTab TAB = FabricItemGroup.builder(new ResourceLocation(Constants.MOD_ID, "items"))
        .icon {-> Items.IRON_INGOT.defaultInstance}
        .displayItems {flags, output, displayOp ->
            for (Supplier<ItemStack> item : TAB_ITEMS) {
                output.accept(item.get())
            }
        }
        .build()

    @Override
    void onInitialize(ModContainer mod) {
        ModularMetalsCommon.init()
    }
}
