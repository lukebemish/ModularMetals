package dev.lukebemish.modularmetals.quilt.platform

import com.google.auto.service.AutoService
import groovy.transform.Memoized
import dev.lukebemish.modularmetals.quilt.ModularMetalsQuilt
import dev.lukebemish.modularmetals.services.IPlatformHelper
import net.fabricmc.api.EnvType
import net.minecraft.world.item.ItemStack
import org.quiltmc.loader.api.QuiltLoader
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader

import java.nio.file.Path
import java.util.function.Supplier

@AutoService(IPlatformHelper)
class PlatformHelperImpl implements IPlatformHelper {

    @Override
    boolean isDevelopmentEnvironment() {
        return QuiltLoader.developmentEnvironment
    }

    @Override
    boolean isClient() {
        return MinecraftQuiltLoader.environmentType == EnvType.CLIENT
    }

    @Override
    Path getConfigFolder() {
        return QuiltLoader.configDir
    }

    @Override
    void addTabItem(Supplier<ItemStack> itemStackSupplier) {
        ModularMetalsQuilt.TAB_ITEMS.add(itemStackSupplier)
    }

    @Override
    Platform getPlatform() {
        return Platform.QUILT
    }

    @Override
    @Memoized
    boolean isModPresent(String modid) {
        return QuiltLoader.isModLoaded(modid)
    }
}
