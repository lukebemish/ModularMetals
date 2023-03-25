package dev.lukebemish.modularmetals.services

import dev.lukebemish.modularmetals.data.MobEffectProvider
import net.minecraft.world.food.FoodProperties
import net.minecraft.world.item.ItemStack

import java.nio.file.Path
import java.util.function.Supplier

interface IPlatformHelper {
    boolean isDevelopmentEnvironment();

    boolean isClient()

    Path getConfigFolder()

    void addTabItem(Supplier<ItemStack> itemStackSupplier)

    Platform getPlatform()

    enum Platform {
        FORGE,
        QUILT
    }

    Set<String> modList()

    FoodProperties platformData(FoodProperties.Builder builder, List<MobEffectProvider> effects)
}
