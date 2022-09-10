package io.github.lukebemish.modularmetals.services

import groovy.transform.AutoFinal
import groovy.transform.CompileStatic
import io.github.lukebemish.modularmetals.Constants

@AutoFinal
@CompileStatic
class Services {
    static final IPlatformHelper PLATFORM = load(IPlatformHelper.class)

    static <T> T load(Class<T> clazz) {
        T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow {new NullPointerException("Failed to load service for ${clazz.getName()}")}
        Constants.LOGGER.debug("Loaded ${loadedService} for service ${clazz}")
        return loadedService
    }
}