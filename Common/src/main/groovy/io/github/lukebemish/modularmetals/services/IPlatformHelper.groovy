package io.github.lukebemish.modularmetals.services

import groovy.transform.CompileStatic
import net.minecraft.world.item.CreativeModeTab

import java.nio.file.Path

@CompileStatic
interface IPlatformHelper {
    boolean isDevelopmentEnvironment();

    boolean isClient()

    Path getConfigFolder()

    CreativeModeTab getItemTab()

    CreativeModeTab getBlockTab()

    Platform getPlatform()

    enum Platform {
        FORGE,
        QUILT
    }

    boolean isModPresent(String modid)
}
