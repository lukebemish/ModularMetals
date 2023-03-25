package dev.lukebemish.modularmetals.data.variant.tool

import com.mojang.serialization.Codec
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import dev.lukebemish.modularmetals.data.Fillable
import net.minecraft.world.item.Item
import net.minecraft.world.item.ShovelItem
import net.minecraft.world.item.Tier

@CodecSerializable(property = "SHOVEL_CODEC")
@TupleConstructor(includeSuperProperties = true, callSuper = true, includeSuperFields = true)
class ShovelVariant extends ToolVariant {
    Fillable<Float> attackModifier
    Fillable<Float> speedModifier

    @Override
    ToolItemSupplier getToolItemSupplier() {
        return { Tier tier, float attack, float speed, Item.Properties properties -> new ShovelItem(tier, attack, speed, properties) }
    }

    @Override
    Codec getCodec() {
        return SHOVEL_CODEC
    }
}
