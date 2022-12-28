package io.github.lukebemish.modularmetals.data.filter

import com.mojang.serialization.Codec
import groovy.transform.CompileStatic
import io.github.groovymc.cgl.api.transform.codec.ExposeCodec
import net.minecraft.resources.ResourceLocation

@Singleton
class AllFilter extends Filter {
    @ExposeCodec
    static final Codec<AllFilter> ALL_CODEC = Codec.unit(AllFilter.instance)

    @Override
    Codec getCodec() {
        return ALL_CODEC
    }

    @Override
    boolean matches(ResourceLocation rl) {
        return true
    }
}
