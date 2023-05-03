package dev.lukebemish.modularmetals.objects

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block

class MMBlockItem extends BlockItem {
    final Optional<CompoundTag> itemTag

    MMBlockItem(Block block, MMItemProps props) {
        super(block, props.props)
        this.itemTag = props.tag
    }

    @Override
    ItemStack getDefaultInstance() {
        return super.getDefaultInstance().tap {
            if (itemTag.isPresent()) it.setTag(itemTag.get())
        }
    }
}
