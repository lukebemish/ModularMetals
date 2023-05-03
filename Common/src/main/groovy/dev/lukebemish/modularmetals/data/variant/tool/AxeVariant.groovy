package dev.lukebemish.modularmetals.data.variant.tool

import com.mojang.serialization.Codec
import dev.lukebemish.modularmetals.data.Fillable
import dev.lukebemish.modularmetals.objects.MMAxeItem
import dev.lukebemish.modularmetals.objects.MMItemProps
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import net.minecraft.world.item.Tier

@CodecSerializable(property = "AXE_CODEC")
@TupleConstructor(includeSuperProperties = true, callSuper = true, includeSuperFields = true)
class AxeVariant extends ToolVariant {
    Fillable<Float> attackModifier
    Fillable<Float> speedModifier

    @Override
    ToolItemSupplier getToolItemSupplier() {
        return { Tier tier, float attack, float speed, MMItemProps properties -> new MMAxeItem(tier, attack, speed, properties) }
    }

    @Override
    Codec getCodec() {
        return AXE_CODEC
    }
}
