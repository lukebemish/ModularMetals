package dev.lukebemish.modularmetals.objects

import groovy.transform.TupleConstructor
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.Item

@TupleConstructor
 class MMItemProps {
    Item.Properties props
    Optional<CompoundTag> tag
}
