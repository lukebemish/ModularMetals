package dev.lukebemish.modularmetals.data


import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import net.minecraft.resources.ResourceLocation

@CodecSerializable
@TupleConstructor
class Category {
    final List<ResourceLocation> variants = []
    final List<ResourceLocation> recipes = []
    final boolean replace = false

    Category merge(Category other) {
        if (other.replace)
            return other
        new Category(this.variants + other.variants, this.recipes + other.recipes, this.replace)
    }

    static final Category EMPTY = new Category([], [], false)
}
