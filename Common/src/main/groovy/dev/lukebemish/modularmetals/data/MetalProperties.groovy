package dev.lukebemish.modularmetals.data

import dev.lukebemish.modularmetals.data.filter.resource.AndResourceFilter
import dev.lukebemish.modularmetals.data.filter.resource.NoneResourceFilter
import dev.lukebemish.modularmetals.data.filter.resource.OrResourceFilter
import dev.lukebemish.modularmetals.data.filter.resource.ResourceFilter
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.StringRepresentable

@CodecSerializable
@CompileStatic
@TupleConstructor
class MetalProperties {
    ResourceFilter metals = NoneResourceFilter.instance
    List<ResourceLocation> categories = []
    Map<ResourceLocation,ResourceLocation> existingVariants = [:]
    ResourceFilter banVariants = NoneResourceFilter.instance
    ResourceFilter banRecipes = NoneResourceFilter.instance
    Map<ResourceLocation,ObjectHolder> properties = [:]
    MergeType replace = MergeType.OR

    enum MergeType implements StringRepresentable {
        REPLACE("replace"),
        OR("or"),
        AND("and")

        final String name

        MergeType(String name) {
            this.name = name
        }

        @Override
        String getSerializedName() {
            return this.name
        }
    }

    MetalProperties mergeProperties(MetalProperties other) {
        if (other.replace == MergeType.REPLACE)
            return other
        categories.addAll(other.categories)
        existingVariants.putAll(other.existingVariants)
        properties.putAll(other.properties)

        if (other.replace == MergeType.OR) {
            banVariants = new OrResourceFilter(List.of(banVariants, other.banVariants))
            banRecipes = new OrResourceFilter(List.of(banRecipes, other.banRecipes))
            metals = new OrResourceFilter(List.of(metals, other.metals))
        } else {
            banVariants = new AndResourceFilter(List.of(banVariants, other.banVariants))
            banRecipes = new AndResourceFilter(List.of(banRecipes, other.banRecipes))
            metals = new AndResourceFilter(List.of(metals, other.metals))
        }

        return this
    }
}
