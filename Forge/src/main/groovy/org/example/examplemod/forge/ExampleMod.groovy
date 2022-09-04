package org.example.examplemod.forge

import com.matyrobbrt.gml.GMod
import groovy.transform.CompileStatic
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import org.example.examplemod.CommonClass
import org.example.examplemod.Constants

@GMod(Constants.MOD_ID)
@CompileStatic
class ExampleMod {
    ExampleMod() {
        Constants.LOG.info("Hello Forge world!")
        CommonClass.init()
        forgeBus.register(this)
    }

    @SubscribeEvent
    void onItemTooltip(ItemTooltipEvent event) {
        CommonClass.onItemTooltip(event.itemStack, event.entity, event.flags, event.toolTip)
    }
}
