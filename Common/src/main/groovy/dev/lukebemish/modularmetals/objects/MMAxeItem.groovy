package dev.lukebemish.modularmetals.objects

import groovy.transform.CompileStatic
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.AxeItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Tier

@CompileStatic
class MMAxeItem extends AxeItem {
    final Optional<CompoundTag> itemTag

    MMAxeItem(Tier tier, float f, float g, MMItemProps properties) {
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
