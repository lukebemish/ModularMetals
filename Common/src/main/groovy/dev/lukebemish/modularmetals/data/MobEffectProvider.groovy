package dev.lukebemish.modularmetals.data

import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectInstance

@CompileStatic
@CodecSerializable
@TupleConstructor
class MobEffectProvider {
    final RegistryProvider effect
    final float probability = 1.0f
    final int duration = 0
    final int amplifier = 0
    final boolean ambient = false
    final boolean visible = true
    final Optional<Boolean> showIcon = Optional.empty()

    MobEffectInstance provide() {
        MobEffect effect = effect.get(BuiltInRegistries.MOB_EFFECT)
        if (effect == null) {
            throw new IllegalArgumentException("Invalid effect: " + this.effect)
        }
        return new MobEffectInstance(effect, duration, amplifier, ambient, visible, showIcon.orElse(visible))
    }
}
