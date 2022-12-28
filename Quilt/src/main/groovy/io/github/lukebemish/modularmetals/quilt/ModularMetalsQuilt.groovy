package io.github.lukebemish.modularmetals.quilt

import groovy.transform.CompileStatic
import io.github.lukebemish.modularmetals.ModularMetalsCommon
import org.quiltmc.loader.api.ModContainer
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer

class ModularMetalsQuilt implements ModInitializer {
    /*static final CreativeModeTab ITEM_TAB = QuiltItemGroup.builder(new ResourceLocation(Constants.MOD_ID,'items')).icon({->
        new ItemStack(Items.IRON_INGOT)
    }).build()
    static final CreativeModeTab BLOCK_TAB = QuiltItemGroup.builder(new ResourceLocation(Constants.MOD_ID,'blocks')).icon({->
        new ItemStack(Items.IRON_BLOCK)
    }).build()*/

    @Override
    void onInitialize(ModContainer mod) {
        ModularMetalsCommon.init()
    }
}
