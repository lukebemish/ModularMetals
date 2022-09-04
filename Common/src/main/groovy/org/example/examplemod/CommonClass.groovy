package org.example.examplemod

import groovy.transform.CompileStatic
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import net.minecraft.world.food.FoodProperties
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import org.example.examplemod.platform.Services
import org.jetbrains.annotations.Nullable

@CompileStatic
class CommonClass {
    static void init() {
        Constants.LOG.info("Hello from Common init on ${Services.PLATFORM.getPlatformName()}! we are currently in a ${Services.PLATFORM.isDevelopmentEnvironment() ? "development" : "production"} environment!")
    }

    static void onItemTooltip(ItemStack stack, @Nullable Player player, TooltipFlag tooltipFlag, List<Component> tooltip) {
        if (!stack.isEmpty()) {
            final FoodProperties food = stack.getItem().getFoodProperties()
            if (food != null) {
                tooltip.add(Component.literal("Nutrition: ${food.getNutrition()}"))
                tooltip.add(Component.literal("Saturation: ${food.getSaturationModifier()}"))
            }
        }
    }
}
