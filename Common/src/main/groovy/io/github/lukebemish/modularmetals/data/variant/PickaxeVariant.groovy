package io.github.lukebemish.modularmetals.data.variant


import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import io.github.groovymc.cgl.api.transform.codec.WithCodec
import io.github.lukebemish.modularmetals.data.Fillable
import net.minecraft.world.item.Item
import net.minecraft.world.item.PickaxeItem
import net.minecraft.world.item.Tier

@CodecSerializable(property = "PICKAXE_CODEC", camelToSnake = true)
@TupleConstructor(includeSuperProperties = true, callSuper = true, includeSuperFields = true)
class PickaxeVariant extends ToolVariant {
    @WithCodec(value = { CODEC.INT.<Float>xmap({(float)it}, {(int)it}) }, target = [0])
    Fillable<Float> attackModifier
    Fillable<Float> speedModifier

    @Override
    ToolItemSupplier getToolItemSupplier() {
        return { Tier tier, float attack, float speed, Item.Properties properties -> new PickaxeItem(tier, (int) attack, speed, properties) }
    }
}
