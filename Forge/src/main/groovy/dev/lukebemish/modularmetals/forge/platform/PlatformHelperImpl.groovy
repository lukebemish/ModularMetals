package dev.lukebemish.modularmetals.forge.platform

import com.google.auto.service.AutoService
import groovy.transform.Memoized
import dev.lukebemish.modularmetals.forge.ModularMetalsForge
import dev.lukebemish.modularmetals.services.IPlatformHelper
import net.minecraft.world.item.ItemStack
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.loading.FMLLoader
import net.minecraftforge.fml.loading.FMLPaths

import java.nio.file.Path
import java.util.function.Supplier

@AutoService(IPlatformHelper)
class PlatformHelperImpl implements IPlatformHelper {

    @Override
    boolean isDevelopmentEnvironment() {
        return !FMLLoader.production
    }

    @Override
    boolean isClient() {
        return FMLLoader.dist == Dist.CLIENT
    }

    @Override
    Path getConfigFolder() {
        return FMLPaths.CONFIGDIR.get()
    }

    @Override
    void addTabItem(Supplier<ItemStack> itemStackSupplier) {
        ModularMetalsForge.TAB_ITEMS.add(itemStackSupplier)
    }

    @Override
    Platform getPlatform() {
        return Platform.FORGE
    }

    @Override
    @Memoized
    boolean isModPresent(String modid) {
        return ModList.get().mods.any {it.modId == modid}
    }
}
