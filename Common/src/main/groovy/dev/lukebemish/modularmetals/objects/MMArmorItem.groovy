package dev.lukebemish.modularmetals.objects

import net.minecraft.world.item.ArmorItem
import net.minecraft.world.item.ArmorMaterial
import net.minecraft.world.item.ItemStack

class MMArmorItem extends ArmorItem {
    final MMItemProps modularProps

    MMArmorItem(ArmorMaterial armorMaterial, Type type, MMItemProps props) {
        super(armorMaterial, type, props.props)
        this.modularProps = props
    }

    @Override
    ItemStack getDefaultInstance() {
        return super.getDefaultInstance().tap {
            if (modularProps.tag.isPresent()) it.setTag(modularProps.tag.get())
        }
    }
}
