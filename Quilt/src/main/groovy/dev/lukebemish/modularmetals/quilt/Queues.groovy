package dev.lukebemish.modularmetals.quilt

import com.mojang.datafixers.util.Pair
import dev.lukebemish.modularmetals.data.MobEffectProvider

import groovy.transform.CompileStatic
import net.minecraft.world.food.FoodProperties

@CompileStatic
class Queues {
    static final List<Pair<FoodProperties, List<MobEffectProvider>>> FOOD_QUEUE = new ArrayList<>()

    static void process() {
        for (def pair : FOOD_QUEUE) {
            pair.getFirst().effects.addAll(pair.getSecond().collect { Pair.of(it.provide(),it.probability) })
        }
    }
}
