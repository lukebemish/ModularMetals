package dev.lukebemish.modularmetals.data.variant.armor

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.Decoder
import dev.lukebemish.modularmetals.Constants
import dev.lukebemish.modularmetals.ModularMetalsCommon
import dev.lukebemish.modularmetals.data.Fillable
import dev.lukebemish.modularmetals.data.Metal
import dev.lukebemish.modularmetals.data.tier.ModularArmorTier
import dev.lukebemish.modularmetals.data.variant.ItemVariant
import dev.lukebemish.modularmetals.util.MoreCodecs
import groovy.transform.CompileStatic
import groovy.transform.Memoized
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.codec.ObjectOps
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import io.github.groovymc.cgl.api.transform.codec.WithCodec
import io.github.groovymc.cgl.reg.RegistryObject
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ArmorItem
import net.minecraft.world.item.Item

@CompileStatic
@CodecSerializable(property = 'ARMOR_CODEC')
@TupleConstructor(includeSuperProperties = true, callSuper = true, includeFields = true)
class ArmorVariant extends ItemVariant {
    @WithCodec(value = { MoreCodecs.ARMOR_TYPE_CODEC }, target = [0])
    Fillable<ArmorItem.Type> armorType

    private static ModularArmorTier getFailedArmorTier() {
        return ((Decoder<ModularArmorTier>) ModularArmorTier.$CODEC).parse(ObjectOps.instance, [:]).getOrThrow(false, {})
    }

    @Memoized static ModularArmorTier getArmorTier(Metal metal, ResourceLocation location) {
        DataResult<ModularArmorTier> result = metal.getPropertyFromMap(new ResourceLocation(Constants.MOD_ID, 'armor_tier'))?.decode(ModularArmorTier.$CODEC)
        return (result?.result()?.orElseGet({->
            Constants.LOGGER.error("Issue loading armor material information for metal ${location}: ${result.error().get().message()}")
            return getFailedArmorTier()
        })?:getFailedArmorTier()).bake(location)
    }

    @Override
    RegistryObject<? extends Item> registerItem(String location, ResourceLocation variantRl, ResourceLocation metalRl, Metal metal, Map props) {
        var armorType = getArmorType().apply(props).getOrThrow(false, {
            Constants.LOGGER.error("Armor type could not be parsed in variant ${variantRl} for metal ${metalRl}")
        })
        return ModularMetalsCommon.ITEMS.register(location, {->
            new ArmorItem(getArmorTier(metal, metalRl), armorType, makeProperties(props))
        })
    }

    @Override
    Codec getCodec() {
        return ARMOR_CODEC
    }
}
