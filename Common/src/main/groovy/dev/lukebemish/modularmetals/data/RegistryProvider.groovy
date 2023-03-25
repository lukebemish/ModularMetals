package dev.lukebemish.modularmetals.data

import com.mojang.serialization.Codec
import groovy.transform.Memoized
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.ExposeCodec
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation

@TupleConstructor
class RegistryProvider {
    final ResourceLocation location

    @ExposeCodec
    public static final Codec<RegistryProvider> CODEC = ResourceLocation.CODEC.xmap(RegistryProvider::new, { it.location })

    @Memoized <T> T get(Registry<T> registry) {
        registry.containsKey(location) ? registry.get(location) : null
    }
}
