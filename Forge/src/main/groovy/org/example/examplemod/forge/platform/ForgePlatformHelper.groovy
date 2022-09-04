package org.example.examplemod.forge.platform

import groovy.transform.CompileStatic
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.loading.FMLLoader
import org.example.examplemod.platform.services.IPlatformHelper

@CompileStatic
class ForgePlatformHelper implements IPlatformHelper {

    @Override
    String getPlatformName() {
        return "Forge"
    }

    @Override
    boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId)
    }

    @Override
    boolean isDevelopmentEnvironment() {
        return !FMLLoader.production
    }
}
