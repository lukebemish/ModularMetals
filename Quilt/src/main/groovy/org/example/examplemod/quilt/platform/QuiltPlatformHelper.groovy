package org.example.examplemod.quilt.platform


import groovy.transform.CompileStatic
import org.example.examplemod.platform.services.IPlatformHelper
import org.quiltmc.loader.api.QuiltLoader

@CompileStatic
class QuiltPlatformHelper implements IPlatformHelper {
    @Override
    String getPlatformName() {
        return "Quilt"
    }

    @Override
    boolean isModLoaded(String modId) {
        return QuiltLoader.isModLoaded(modId)
    }

    @Override
    boolean isDevelopmentEnvironment() {
        return QuiltLoader.developmentEnvironment
    }
}
