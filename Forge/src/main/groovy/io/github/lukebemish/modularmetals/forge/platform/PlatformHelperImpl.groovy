package io.github.lukebemish.modularmetals.forge.platform

import com.google.auto.service.AutoService
import groovy.transform.CompileStatic
import groovy.transform.Memoized
import io.github.lukebemish.modularmetals.services.IPlatformHelper
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.loading.FMLLoader
import net.minecraftforge.fml.loading.FMLPaths

import java.nio.file.Path

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

    /*@Override
    CreativeModeTab getItemTab() {
        return ModularMetalsForge.ITEM_TAB
    }

    @Override
    CreativeModeTab getBlockTab() {
        return ModularMetalsForge.BLOCK_TAB
    }*/

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
