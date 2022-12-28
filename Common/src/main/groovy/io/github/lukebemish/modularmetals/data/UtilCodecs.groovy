package io.github.lukebemish.modularmetals.data

import com.google.common.base.Suppliers
import com.google.gson.JsonElement
import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import groovy.transform.CompileStatic
import io.github.lukebemish.modularmetals.util.OpsCodec
import net.minecraft.world.item.crafting.Ingredient

import java.util.function.Supplier

class UtilCodecs {
    static final Codec<Supplier<Ingredient>> INGREDIENT_CODEC = new OpsCodec<JsonElement>(JsonOps.INSTANCE).<Supplier<Ingredient>>xmap({ Suppliers.memoize({->Ingredient.fromJson(it)})},{it.get().toJson()})

    static <O> Codec<List<O>> singleOrList(Codec<O> codec) {
        return Codec.either(codec, codec.listOf()).<List<O>>xmap({either ->
            either.<List<O>>map({
                List.of(it)
            },{
                it
            })
        },{
            Either<O, List<O>>.right(it)
        })
    }
}
