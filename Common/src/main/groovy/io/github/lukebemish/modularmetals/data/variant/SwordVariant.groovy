package io.github.lukebemish.modularmetals.data.variant

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import io.github.lukebemish.groovyduvet.wrapper.minecraft.api.codec.CodecSerializable
import io.github.lukebemish.groovyduvet.wrapper.minecraft.api.codec.WithCodec
import net.minecraft.world.item.Item
import net.minecraft.world.item.SwordItem
import net.minecraft.world.item.Tier

@CompileStatic
@CodecSerializable(property = "SWORD_CODEC")
@TupleConstructor(includeSuperProperties = true, callSuper = true)
class SwordVariant extends ToolVariant {
    @WithCodec({ CODEC.INT.<Float>xmap({(float)it}, {(int)it}) })
    float attackModifier
    float speedModifier

    @Override
    ToolItemSupplier getToolItemSupplier() {
        return { Tier tier, float attack, float speed, Item.Properties properties -> new SwordItem(tier, (int) attack, speed, properties) }
    }
}
