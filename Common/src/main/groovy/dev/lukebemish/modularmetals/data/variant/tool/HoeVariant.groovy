package dev.lukebemish.modularmetals.data.variant.tool

import com.mojang.serialization.Codec
import dev.lukebemish.modularmetals.data.Fillable
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import io.github.groovymc.cgl.api.transform.codec.WithCodec
import net.minecraft.world.item.HoeItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.Tier

@CodecSerializable(property = "HOE_CODEC")
@TupleConstructor(includeSuperProperties = true, callSuper = true, includeSuperFields = true)
class HoeVariant extends ToolVariant {
    @WithCodec(value = { Codec.INT.<Float>xmap({(float)it}, {(int)it}) }, target = [0])
    Fillable<Float> attackModifier
    Fillable<Float> speedModifier

    @Override
    ToolItemSupplier getToolItemSupplier() {
        return { Tier tier, float attack, float speed, Item.Properties properties -> new HoeItem(tier, (int)attack, speed, properties) }
    }

    @Override
    Codec getCodec() {
        return HOE_CODEC
    }
}