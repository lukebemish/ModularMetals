package io.github.lukebemish.modularmetals.services

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

    boolean isModPresent(String modid)
}
