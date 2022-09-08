package io.github.lukebemish.modularmetals.data.filter

import com.mojang.serialization.Codec
import groovy.transform.CompileStatic
import io.github.lukebemish.groovyduvet.wrapper.minecraft.api.codec.ExposeCodec
import net.minecraft.resources.ResourceLocation

@CompileStatic
@Singleton
class AllFilter extends Filter {
    @ExposeCodec
    static Codec<AllFilter> CODEC = Codec.unit(AllFilter.instance)

    @Override
    Codec getCodec() {
        return CODEC
    }

    @Override
    boolean matches(ResourceLocation rl) {
        return true
    }
}
