package io.github.lukebemish.modularmetals.util

import com.mojang.serialization.Codec
import groovy.transform.CompileStatic

@CompileStatic
interface CodecAware<O> {
    Codec<? extends O> getCodec()
}