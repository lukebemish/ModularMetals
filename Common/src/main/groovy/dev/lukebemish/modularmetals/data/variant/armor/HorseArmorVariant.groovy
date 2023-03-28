package dev.lukebemish.modularmetals.data.variant.armor

import com.mojang.serialization.Codec
import dev.lukebemish.modularmetals.Constants
import dev.lukebemish.modularmetals.ModularMetalsCommon
import dev.lukebemish.modularmetals.client.variant.ClientVariantHandler
import dev.lukebemish.modularmetals.client.variant.HorseArmorClientVariantHandler
import dev.lukebemish.modularmetals.data.Metal
import dev.lukebemish.modularmetals.data.variant.ItemVariant
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import io.github.groovymc.cgl.reg.RegistryObject
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ArmorItem
import net.minecraft.world.item.HorseArmorItem
import net.minecraft.world.item.Item

@CompileStatic
@CodecSerializable(property = 'HORSE_ARMOR_CODEC')
@TupleConstructor(includeSuperProperties = true, callSuper = true, includeSuperFields = true)
class HorseArmorVariant extends ItemVariant {

    @Override
    RegistryObject<? extends Item> registerItem(String location, ResourceLocation variantRl, ResourceLocation metalRl, Metal metal, Map props) {
        return ModularMetalsCommon.ITEMS.register(location, {->
            var armorTier = ArmorVariant.getArmorTier(metal, metalRl)
            int protection = armorTier.getDefenseForType(ArmorItem.Type.CHESTPLATE)
            new HorseArmorItem(protection, "${Constants.MOD_ID}__$location", makeProperties(props))
        })
    }

    @Override
    Codec getCodec() {
        return HORSE_ARMOR_CODEC
    }

    @Override
    ClientVariantHandler getClientHandler() {
        return new HorseArmorClientVariantHandler()
    }
}
