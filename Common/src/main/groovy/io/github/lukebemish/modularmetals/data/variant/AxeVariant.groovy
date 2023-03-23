package io.github.lukebemish.modularmetals.data.variant


import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import io.github.lukebemish.modularmetals.data.Fillable
import net.minecraft.world.item.AxeItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.Tier

@CodecSerializable(property = "AXE_CODEC")
@TupleConstructor(includeSuperProperties = true, callSuper = true, includeSuperFields = true)
class AxeVariant extends ToolVariant {
    Fillable<Float> attackModifier
    Fillable<Float> speedModifier

    @Override
    ToolItemSupplier getToolItemSupplier() {
        return { Tier tier, float attack, float speed, Item.Properties properties -> new AxeItem(tier, attack, speed, properties) }
    }
}
