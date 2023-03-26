package dev.lukebemish.modularmetals.data

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import net.minecraft.resources.ResourceLocation

@CodecSerializable
@CompileStatic
@TupleConstructor
class MetalProperties {
    List<ResourceLocation> categories = []
    Map<ResourceLocation,ResourceLocation> existingVariants = [:]
    List<ResourceLocation> banVariants = []
    List<ResourceLocation> banRecipes = []
    Map<ResourceLocation,ObjectHolder> properties = [:]

    void mergeProperties(MetalProperties props) {
        categories.addAll(props.categories)
        existingVariants.putAll(props.existingVariants)
        banVariants.addAll(props.banVariants)
        banRecipes.addAll(props.banRecipes)
        properties.putAll(props.properties)
    }
}
