package dev.lukebemish.modularmetals.data.filter

import net.minecraft.resources.ResourceLocation

interface FilterFinder<T> {
    boolean isTag(T thing, ResourceLocation tag)
    boolean isLocation(T thing, ResourceLocation location)
}
