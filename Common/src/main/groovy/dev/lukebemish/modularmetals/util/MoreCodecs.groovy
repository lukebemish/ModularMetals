package dev.lukebemish.modularmetals.util

import com.google.common.base.Suppliers
import com.google.common.collect.BiMap
import com.google.gson.JsonElement
import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.lukebemish.modularmetals.transform.InstanceMap
import groovy.transform.CompileStatic
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.material.Material
import net.minecraft.world.level.material.MaterialColor
import net.minecraft.world.level.material.PushReaction

import java.util.function.Supplier

@CompileStatic
final class MoreCodecs {
    public static final Codec<Supplier<Ingredient>> INGREDIENT_CODEC = new OpsCodec<JsonElement>(JsonOps.INSTANCE).<Supplier<Ingredient>>xmap({ Suppliers.memoize({->Ingredient.fromJson(it)})},{it.get().toJson()})

    private MoreCodecs() {}

    @SuppressWarnings('GrFinalVariableAccess')
    @InstanceMap
    public static final BiMap<String, Material> MATERIAL_MAP

    @SuppressWarnings('GrFinalVariableAccess')
    @InstanceMap
    public static final BiMap<String, MaterialColor> MATERIAL_COLOR_MAP

    @SuppressWarnings('GrFinalVariableAccess')
    @InstanceMap
    public static final BiMap<String, PushReaction> PUSH_REACTION_MAP

    static <T> Codec<T> ofMapCodec(BiMap<String, T> map, String name) {
        return Codec.STRING.<T>flatXmap({ it ->
            var out = map.get(it)
            if (out == null) {
                return DataResult.<T>error {->"Unknown $name: $it"}
            }
            return DataResult.<T>success(out)
        }, {
            var out = map.inverse().get(it)
            if (out == null) {
                return DataResult.<String>error {->"Unknown $name: $it"}
            }
            return DataResult.<String>success(out)
        })
    }

    public static final Codec<PushReaction> PUSH_REACTION_CODEC = ofMapCodec(PUSH_REACTION_MAP, "push_reaction")
    public static final Codec<MaterialColor> MATERIAL_COLOR_CODEC = Codec.<MaterialColor,MaterialColor>either(
        ofMapCodec(MATERIAL_COLOR_MAP, "material_color"),
        intCodecBounded(0, 61).<MaterialColor>xmap({MaterialColor.byId(it)}, {it.id})
    ).<MaterialColor>xmap({ it.map({it},{it}) }, { Either.<MaterialColor,MaterialColor>left(it) })

    public static final Codec<Material> MATERIAL_NAMED_CODEC = ofMapCodec(MATERIAL_MAP, "material")
    public static final Codec<Material> MATERIAL_DIRECT_CODEC = RecordCodecBuilder.<Material>create {
        it.<MaterialColor, Boolean, Boolean, Boolean, Boolean, Boolean, Boolean, PushReaction>group(
            MATERIAL_COLOR_CODEC.optionalFieldOf("material_color", MaterialColor.NONE).forGetter(Material::getColor),
            Codec.BOOL.optionalFieldOf("flammable", false).forGetter(Material::isFlammable),
            Codec.BOOL.optionalFieldOf("liquid", false).forGetter(Material::isLiquid),
            Codec.BOOL.optionalFieldOf("solid", true).forGetter(Material::isSolid),
            Codec.BOOL.optionalFieldOf("blocks_motion", true).forGetter(Material::blocksMotion),
            Codec.BOOL.optionalFieldOf("solid_blocking", true).forGetter(Material::isSolidBlocking),
            Codec.BOOL.optionalFieldOf("replaceable", false).forGetter(Material::isReplaceable),
            PUSH_REACTION_CODEC.optionalFieldOf("push_reaction", PushReaction.NORMAL).forGetter(Material::getPushReaction)
        ).<Material>apply(it, { color, flammable, liquid, solid, blocksMotion, solidBlocking, replaceable, pushReaction ->
            new Material(color, liquid, solid, blocksMotion, solidBlocking, flammable, replaceable, pushReaction)
        })
    }
    public static final Codec<Material> MATERIAL_CODEC = Codec.<Material, Material>either(MATERIAL_NAMED_CODEC, MATERIAL_DIRECT_CODEC).<Material>xmap({ it.map({it},{it}) }, { Either.<Material,Material>left(it) })

    static Codec<Integer> intCodecBounded(int min, int max) {
        return Codec.INT.<Integer>flatXmap({ it ->
            if (it < min || it > max) {
                return DataResult.<Integer>error {->"Value must be between $min and $max: $it"}
            }
            return DataResult.<Integer>success(it)
        }, {
            if (it < min || it > max) {
                return DataResult.<Integer>error {->"Value must be between $min and $max: $it"}
            }
            return DataResult.<Integer>success(it)
        })
    }

    @SuppressWarnings('GrFinalVariableAccess')
    @InstanceMap
    public static final BiMap<String, SoundType> SOUND_TYPE_MAP

    public static final Codec<SoundType> SOUND_TYPE_NAMED_CODEC = ofMapCodec(SOUND_TYPE_MAP, "sound_type")

    static <O> Codec<List<O>> singleOrList(Codec<O> codec) {
        return Codec.either(codec, codec.listOf()).<List<O>>xmap({ either ->
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
