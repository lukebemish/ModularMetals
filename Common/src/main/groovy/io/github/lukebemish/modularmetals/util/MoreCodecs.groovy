package io.github.lukebemish.modularmetals.util

import com.google.common.collect.BiMap
import com.google.common.collect.ImmutableBiMap
import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.world.level.material.Material
import net.minecraft.world.level.material.MaterialColor
import net.minecraft.world.level.material.PushReaction

final class MoreCodecs {
    private MoreCodecs() {}

    static final BiMap<String, Material> MATERIAL_MAP = ImmutableBiMap.copyOf([
        air: Material.AIR,
        structural_air: Material.STRUCTURAL_AIR,
        portal: Material.PORTAL,
        cloth_decoration: Material.CLOTH_DECORATION,
        plant: Material.PLANT,
        water_plant: Material.WATER_PLANT,
        replaceable_plant: Material.REPLACEABLE_PLANT,
        replaceable_fireproof_plant: Material.REPLACEABLE_FIREPROOF_PLANT,
        replaceable_water_plant: Material.REPLACEABLE_WATER_PLANT,
        water: Material.WATER,
        bubble_column: Material.BUBBLE_COLUMN,
        lava: Material.LAVA,
        top_snow: Material.TOP_SNOW,
        fire: Material.FIRE,
        decoration: Material.DECORATION,
        web: Material.WEB,
        sculk: Material.SCULK,
        buildable_glass: Material.BUILDABLE_GLASS,
        clay: Material.CLAY,
        dirt: Material.DIRT,
        grass: Material.GRASS,
        ice_solid: Material.ICE_SOLID,
        sand: Material.SAND,
        sponge: Material.SPONGE,
        shulker_shell: Material.SHULKER_SHELL,
        wood: Material.WOOD,
        nether_wood: Material.NETHER_WOOD,
        bamboo_sapling: Material.BAMBOO_SAPLING,
        bamboo: Material.BAMBOO,
        wool: Material.WOOL,
        explosive: Material.EXPLOSIVE,
        leaves: Material.LEAVES,
        glass: Material.GLASS,
        ice: Material.ICE,
        cactus: Material.CACTUS,
        stone: Material.STONE,
        metal: Material.METAL,
        snow: Material.SNOW,
        heavy_metal: Material.HEAVY_METAL,
        barrier: Material.BARRIER,
        piston: Material.PISTON,
        moss: Material.MOSS,
        vegetable: Material.VEGETABLE,
        egg: Material.EGG,
        cake: Material.CAKE,
        amethyst: Material.AMETHYST,
        powder_snow: Material.POWDER_SNOW,
        frogspawn: Material.FROGSPAWN,
        froglight: Material.FROGLIGHT,
    ])

    static final BiMap<String, MaterialColor> MATERIAL_COLOR_MAP = ImmutableBiMap.copyOf([
        none: MaterialColor.NONE,
        grass: MaterialColor.GRASS,
        sand: MaterialColor.SAND,
        wool: MaterialColor.WOOL,
        fire: MaterialColor.FIRE,
        ice: MaterialColor.ICE,
        metal: MaterialColor.METAL,
        plant: MaterialColor.PLANT,
        snow: MaterialColor.SNOW,
        clay: MaterialColor.CLAY,
        dirt: MaterialColor.DIRT,
        stone: MaterialColor.STONE,
        water: MaterialColor.WATER,
        wood: MaterialColor.WOOD,
        quartz: MaterialColor.QUARTZ,
        color_orange: MaterialColor.COLOR_ORANGE,
        color_magenta: MaterialColor.COLOR_MAGENTA,
        color_light_blue: MaterialColor.COLOR_LIGHT_BLUE,
        color_yellow: MaterialColor.COLOR_YELLOW,
        color_light_green: MaterialColor.COLOR_LIGHT_GREEN,
        color_pink: MaterialColor.COLOR_PINK,
        color_gray: MaterialColor.COLOR_GRAY,
        color_light_gray: MaterialColor.COLOR_LIGHT_GRAY,
        color_cyan: MaterialColor.COLOR_CYAN,
        color_purple: MaterialColor.COLOR_PURPLE,
        color_blue: MaterialColor.COLOR_BLUE,
        color_brown: MaterialColor.COLOR_BROWN,
        color_green: MaterialColor.COLOR_GREEN,
        color_red: MaterialColor.COLOR_RED,
        color_black: MaterialColor.COLOR_BLACK,
        gold: MaterialColor.GOLD,
        diamond: MaterialColor.DIAMOND,
        lapis: MaterialColor.LAPIS,
        emerald: MaterialColor.EMERALD,
        podzol: MaterialColor.PODZOL,
        nether: MaterialColor.NETHER,
        terracotta_white: MaterialColor.TERRACOTTA_WHITE,
        terracotta_orange: MaterialColor.TERRACOTTA_ORANGE,
        terracotta_magenta: MaterialColor.TERRACOTTA_MAGENTA,
        terracotta_light_blue: MaterialColor.TERRACOTTA_LIGHT_BLUE,
        terracotta_yellow: MaterialColor.TERRACOTTA_YELLOW,
        terracotta_light_green: MaterialColor.TERRACOTTA_LIGHT_GREEN,
        terracotta_pink: MaterialColor.TERRACOTTA_PINK,
        terracotta_gray: MaterialColor.TERRACOTTA_GRAY,
        terracotta_light_gray: MaterialColor.TERRACOTTA_LIGHT_GRAY,
        terracotta_cyan: MaterialColor.TERRACOTTA_CYAN,
        terracotta_purple: MaterialColor.TERRACOTTA_PURPLE,
        terracotta_blue: MaterialColor.TERRACOTTA_BLUE,
        terracotta_brown: MaterialColor.TERRACOTTA_BROWN,
        terracotta_green: MaterialColor.TERRACOTTA_GREEN,
        terracotta_red: MaterialColor.TERRACOTTA_RED,
        terracotta_black: MaterialColor.TERRACOTTA_BLACK,
        crimson_nylium: MaterialColor.CRIMSON_NYLIUM,
        crimson_stem: MaterialColor.CRIMSON_STEM,
        crimson_hyphae: MaterialColor.CRIMSON_HYPHAE,
        warped_nylium: MaterialColor.WARPED_NYLIUM,
        warped_stem: MaterialColor.WARPED_STEM,
        warped_hyphae: MaterialColor.WARPED_HYPHAE,
        warped_wart_block: MaterialColor.WARPED_WART_BLOCK,
        deepslate: MaterialColor.DEEPSLATE,
        raw_iron: MaterialColor.RAW_IRON,
        glow_lichen: MaterialColor.GLOW_LICHEN
    ])

    static final BiMap<String, PushReaction> PUSH_REACTION_MAP = ImmutableBiMap.copyOf([
        normal: PushReaction.NORMAL,
        destroy: PushReaction.DESTROY,
        block: PushReaction.BLOCK,
        ignore: PushReaction.IGNORE,
        push_only: PushReaction.PUSH_ONLY
    ])

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

    static final Codec<PushReaction> PUSH_REACTION_CODEC = ofMapCodec(PUSH_REACTION_MAP, "push_reaction")
    static final Codec<MaterialColor> MATERIAL_COLOR_CODEC = Codec.<MaterialColor,MaterialColor>either(
        ofMapCodec(MATERIAL_COLOR_MAP, "material_color"),
        intCodecBounded(0, 61).<MaterialColor>xmap({MaterialColor.byId(it)}, {it.id})
    ).<MaterialColor>xmap({ it.map({it},{it}) }, { Either.<MaterialColor,MaterialColor>left(it) })

    static final Codec<Material> MATERIAL_NAMED_CODEC = ofMapCodec(MATERIAL_MAP, "material")
    static final Codec<Material> MATERIAL_DIRECT_CODEC = RecordCodecBuilder.<Material>create {
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
    static final Codec<Material> MATERIAL_CODEC = Codec.<Material, Material>either(MATERIAL_NAMED_CODEC, MATERIAL_DIRECT_CODEC).<Material>xmap({ it.map({it},{it}) }, { Either.<Material,Material>left(it) })

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
}
