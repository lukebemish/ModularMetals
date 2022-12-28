package io.github.lukebemish.modularmetals.data.variant

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import net.minecraft.world.item.Item
import net.minecraft.world.item.ShovelItem
import net.minecraft.world.item.Tier

@CodecSerializable(property = "SHOVEL_CODEC")
@TupleConstructor(includeSuperProperties = true, callSuper = true, includeSuperFields = true)
class ShovelVariant extends ToolVariant {
    float attackModifier
    float speedModifier

    @Override
    ToolItemSupplier getToolItemSupplier() {
        return { Tier tier, float attack, float speed, Item.Properties properties -> new ShovelItem(tier, attack, speed, properties) }
    }
}
