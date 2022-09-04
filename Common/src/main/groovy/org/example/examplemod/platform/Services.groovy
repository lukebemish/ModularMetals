package org.example.examplemod.platform

import groovy.transform.AutoFinal
import groovy.transform.CompileStatic
import org.example.examplemod.Constants
import org.example.examplemod.platform.services.IPlatformHelper

@AutoFinal
@CompileStatic
class Services {
    static final IPlatformHelper PLATFORM = load(IPlatformHelper.class)

    static <T> T load(Class<T> clazz) {
        T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow {new NullPointerException("Failed to load service for ${clazz.getName()}")}
        Constants.LOG.debug("Loaded ${loadedService} for service ${clazz}")
        return loadedService
    }
}
