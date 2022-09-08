package io.github.lukebemish.modularmetals.data.variant

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import io.github.lukebemish.groovyduvet.wrapper.minecraft.api.codec.CodecSerializable
import net.minecraft.world.item.AxeItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.Tier

@CompileStatic
@CodecSerializable(property = "AXE_CODEC")
@TupleConstructor(includeSuperProperties = true, callSuper = true)
class AxeVariant extends ToolVariant {
    float attackModifier
    float speedModifier

    @Override
    ToolItemSupplier getToolItemSupplier() {
        return { Tier tier, float attack, float speed, Item.Properties properties -> new AxeItem(tier, attack, speed, properties) }
    }
}
