package dev.lukebemish.modularmetals.data

import dev.lukebemish.modularmetals.ModularMetalsCommon
import groovy.transform.CompileStatic
import groovy.transform.ToString
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import net.minecraft.resources.ResourceLocation

@CodecSerializable
@CompileStatic
@TupleConstructor
@ToString(includes = ["variants", "recipes", "inherits", "replace"])
class Category {
    final List<ResourceLocation> variants = []
    final List<ResourceLocation> recipes = []
    final List<ResourceLocation> inherits = []
    final boolean replace = false

    Category merge(Category other) {
        if (other.replace)
            return other
        new Category(this.variants + other.variants, this.recipes + other.recipes, this.inherits+other.inherits, this.replace)
    }

    static final Category EMPTY = new Category([], [], [], false)

    List<ResourceLocation> getFullVariants() {
        var thisRl = ModularMetalsCommon.config.categories.inverse().get(this)
        if (thisRl == null)
            throw new IllegalStateException("Category not registered")
        return getFullVariantsInternal([], thisRl)
    }

    List<ResourceLocation> getFullRecipes() {
        var thisRl = ModularMetalsCommon.config.categories.inverse().get(this)
        if (thisRl == null)
            throw new IllegalStateException("Category not registered")
        return getFullRecipesInternal([], thisRl)
    }

    protected List<ResourceLocation> getFullVariantsInternal(List<ResourceLocation> stack, ResourceLocation thisRl) {
        if (stack.contains(thisRl))
            throw new IllegalStateException("Circular category inheritance detected")
        stack = stack + [thisRl]
        var result = variants
        for (def inherit : inherits) {
            var category = ModularMetalsCommon.config.categories.get(inherit)
            if (category == null)
                throw new IllegalStateException("Category '${inherit}' not registered")
            result = result + category.getFullVariantsInternal(stack, inherit)
        }
        return result
    }

    protected List<ResourceLocation> getFullRecipesInternal(List<ResourceLocation> stack, ResourceLocation thisRl) {
        if (stack.contains(thisRl))
            throw new IllegalStateException("Circular category inheritance detected")
        stack = stack + [thisRl]
        var result = recipes
        for (def inherit : inherits) {
            var category = ModularMetalsCommon.config.categories.get(inherit)
            if (category == null)
                throw new IllegalStateException("Category '${inherit}' not registered")
            result = result + category.getFullRecipesInternal(stack, inherit)
        }
        return result
    }
}
