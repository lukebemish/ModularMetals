package dev.lukebemish.modularmetals.data.filter.resource

import net.minecraft.resources.ResourceLocation

interface ResourceFilterFinder<T> {
    boolean isTag(T thing, ResourceLocation tag)
    boolean isLocation(T thing, ResourceLocation location)
}
