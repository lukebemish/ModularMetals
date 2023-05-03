package dev.lukebemish.modularmetals.objects

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.PickaxeItem
import net.minecraft.world.item.Tier

class MMPickaxeItem extends PickaxeItem {
    final Optional<CompoundTag> itemTag

    MMPickaxeItem(Tier tier, int f, float g, MMItemProps properties) {
        super(tier, f, g, properties.props)
        this.itemTag = properties.tag
    }

    @Override
    ItemStack getDefaultInstance() {
        return super.getDefaultInstance().tap {
            if (itemTag.isPresent()) it.setTag(itemTag.get())
        }
    }
}
