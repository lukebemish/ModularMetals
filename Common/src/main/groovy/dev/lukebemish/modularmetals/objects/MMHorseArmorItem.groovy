package dev.lukebemish.modularmetals.objects

import net.minecraft.world.item.HorseArmorItem
import net.minecraft.world.item.ItemStack

class MMHorseArmorItem extends HorseArmorItem {
    final MMItemProps modularProps

    MMHorseArmorItem(int i, String string, MMItemProps props) {
        super(i, string, props.props)
        this.modularProps = props
    }

    @Override
    ItemStack getDefaultInstance() {
        return super.getDefaultInstance().tap {
            if (modularProps.tag.isPresent()) it.setTag(modularProps.tag.get())
        }
    }
}
