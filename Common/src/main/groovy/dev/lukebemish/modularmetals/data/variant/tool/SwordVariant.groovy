package dev.lukebemish.modularmetals.data.variant.tool

import com.mojang.serialization.Codec
import dev.lukebemish.modularmetals.data.Fillable
import dev.lukebemish.modularmetals.objects.MMItemProps
import dev.lukebemish.modularmetals.objects.MMSwordItem
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import io.github.groovymc.cgl.api.transform.codec.WithCodec
import net.minecraft.world.item.Tier

@CodecSerializable(property = "SWORD_CODEC")
@TupleConstructor(includeSuperProperties = true, callSuper = true, includeSuperFields = true)
class SwordVariant extends ToolVariant {
    @WithCodec(value = { Codec.INT.<Float>xmap({(float)it}, {(int)it}) }, target = [0])
    Fillable<Float> attackModifier
    Fillable<Float> speedModifier

    @Override
    ToolItemSupplier getToolItemSupplier() {
        return { Tier tier, float attack, float speed, MMItemProps properties -> new MMSwordItem(tier, (int) attack, speed, properties) }
    }

    @Override
    Codec getCodec() {
        return SWORD_CODEC
    }
}
