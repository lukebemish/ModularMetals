package dev.lukebemish.modularmetals.quilt

import dev.lukebemish.modularmetals.Constants
import dev.lukebemish.modularmetals.ModularMetalsCommon
import groovy.transform.CompileStatic
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer

import java.util.function.Supplier

@CompileStatic
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

        Queues.process()
    }
}
