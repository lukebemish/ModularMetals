package dev.lukebemish.modularmetals.objects

import groovy.transform.CompileStatic
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

@CompileStatic
class MMItem extends Item {
    final Optional<CompoundTag> itemTag

    MMItem(MMItemProps props) {
        super(props.props)
        this.itemTag = props.tag
    }

    @Override
    ItemStack getDefaultInstance() {
        return super.getDefaultInstance().tap {
            if (itemTag.isPresent()) it.setTag(itemTag.get())
        }
    }
}
