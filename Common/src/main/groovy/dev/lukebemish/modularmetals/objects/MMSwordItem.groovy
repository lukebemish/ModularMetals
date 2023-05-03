package dev.lukebemish.modularmetals.objects

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.SwordItem
import net.minecraft.world.item.Tier

class MMSwordItem extends SwordItem {
    final Optional<CompoundTag> itemTag

    MMSwordItem(Tier tier, int f, float g, MMItemProps properties) {
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
