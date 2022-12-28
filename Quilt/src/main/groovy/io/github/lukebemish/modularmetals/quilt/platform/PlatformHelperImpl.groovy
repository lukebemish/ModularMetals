package io.github.lukebemish.modularmetals.quilt.platform

import com.google.auto.service.AutoService
import groovy.transform.CompileStatic
import groovy.transform.Memoized
import io.github.lukebemish.modularmetals.quilt.ModularMetalsQuilt
import io.github.lukebemish.modularmetals.services.IPlatformHelper
import net.fabricmc.api.EnvType
import net.minecraft.world.item.CreativeModeTab
import org.quiltmc.loader.api.QuiltLoader
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader

import java.nio.file.Path

@CompileStatic
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
    CreativeModeTab getItemTab() {
        return ModularMetalsQuilt.ITEM_TAB
    }

    @Override
    CreativeModeTab getBlockTab() {
        return ModularMetalsQuilt.BLOCK_TAB
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
