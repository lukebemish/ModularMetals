package io.github.lukebemish.modularmetals.data.recipe

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import io.github.lukebemish.groovyduvet.wrapper.minecraft.api.codec.CodecSerializable
import io.github.lukebemish.modularmetals.data.MapHolder

@CompileStatic
@CodecSerializable(camelToSnake = true)
@TupleConstructor
class Recipe {
    MapHolder template
}
