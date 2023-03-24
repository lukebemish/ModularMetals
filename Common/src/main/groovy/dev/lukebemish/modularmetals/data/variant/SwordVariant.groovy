package dev.lukebemish.modularmetals.data.variant


import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import io.github.groovymc.cgl.api.transform.codec.WithCodec
import dev.lukebemish.modularmetals.data.Fillable
import net.minecraft.world.item.Item
import net.minecraft.world.item.SwordItem
import net.minecraft.world.item.Tier

@CodecSerializable(property = "SWORD_CODEC")
@TupleConstructor(includeSuperProperties = true, callSuper = true, includeSuperFields = true)
class SwordVariant extends ToolVariant {
    @WithCodec(value = { CODEC.INT.<Float>xmap({(float)it}, {(int)it}) }, target = [0])
    Fillable<Float> attackModifier
    Fillable<Float> speedModifier

    @Override
    ToolItemSupplier getToolItemSupplier() {
        return { Tier tier, float attack, float speed, Item.Properties properties -> new SwordItem(tier, (int) attack, speed, properties) }
    }
}
