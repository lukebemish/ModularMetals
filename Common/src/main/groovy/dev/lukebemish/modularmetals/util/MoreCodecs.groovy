package dev.lukebemish.modularmetals.util

import com.google.common.collect.BiMap
import com.google.common.collect.ImmutableBiMap
import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.world.level.block.SoundType
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

    static final BiMap<String, SoundType> SOUND_TYPE_MAP = ImmutableBiMap.copyOf([
        wood: SoundType.WOOD,
        gravel: SoundType.GRAVEL,
        grass: SoundType.GRASS,
        lily_pad: SoundType.LILY_PAD,
        stone: SoundType.STONE,
        metal: SoundType.METAL,
        glass: SoundType.GLASS,
        wool: SoundType.WOOL,
        sand: SoundType.SAND,
        snow: SoundType.SNOW,
        powder_snow: SoundType.POWDER_SNOW,
        ladder: SoundType.LADDER,
        anvil: SoundType.ANVIL,
        slime_block: SoundType.SLIME_BLOCK,
        honey_block: SoundType.HONEY_BLOCK,
        wet_grass: SoundType.WET_GRASS,
        coral_block: SoundType.CORAL_BLOCK,
        bamboo: SoundType.BAMBOO,
        bamboo_sapling: SoundType.BAMBOO_SAPLING,
        scaffolding: SoundType.SCAFFOLDING,
        sweet_berry_bush: SoundType.SWEET_BERRY_BUSH,
        crop: SoundType.CROP,
        hard_crop: SoundType.HARD_CROP,
        vine: SoundType.VINE,
        nether_wart: SoundType.NETHER_WART,
        lantern: SoundType.LANTERN,
        stem: SoundType.STEM,
        nylium: SoundType.NYLIUM,
        fungus: SoundType.FUNGUS,
        roots: SoundType.ROOTS,
        shroomlight: SoundType.SHROOMLIGHT,
        weeping_vines: SoundType.WEEPING_VINES,
        twisting_vines: SoundType.TWISTING_VINES,
        soul_sand: SoundType.SOUL_SAND,
        soul_soil: SoundType.SOUL_SOIL,
        basalt: SoundType.BASALT,
        wart_block: SoundType.WART_BLOCK,
        netherrack: SoundType.NETHERRACK,
        nether_bricks: SoundType.NETHER_BRICKS,
        nether_sprouts: SoundType.NETHER_SPROUTS,
        nether_ore: SoundType.NETHER_ORE,
        bone_block: SoundType.BONE_BLOCK,
        netherite_block: SoundType.NETHERITE_BLOCK,
        ancient_debris: SoundType.ANCIENT_DEBRIS,
        loadstone: SoundType.LODESTONE,
        chain: SoundType.CHAIN,
        nether_gold_ore: SoundType.NETHER_GOLD_ORE,
        gilded_blackstone: SoundType.GILDED_BLACKSTONE,
        candle: SoundType.CANDLE,
        amethyst: SoundType.AMETHYST,
        amethyst_cluster: SoundType.AMETHYST_CLUSTER,
        small_amethyst_bud: SoundType.SMALL_AMETHYST_BUD,
        medium_amethyst_bud: SoundType.MEDIUM_AMETHYST_BUD,
        large_amethyst_bud: SoundType.LARGE_AMETHYST_BUD,
        tuff: SoundType.TUFF,
        calcite: SoundType.CALCITE,
        dripstone_block: SoundType.DRIPSTONE_BLOCK,
        pointed_dripstone: SoundType.POINTED_DRIPSTONE,
        copper: SoundType.COPPER,
        cave_vines: SoundType.CAVE_VINES,
        spore_blossom: SoundType.SPORE_BLOSSOM,
        azalea: SoundType.AZALEA,
        flowering_azalea: SoundType.FLOWERING_AZALEA,
        moss_carpet: SoundType.MOSS_CARPET,
        pink_petals: SoundType.PINK_PETALS,
        moss: SoundType.MOSS,
        big_dripleaf: SoundType.BIG_DRIPLEAF,
        small_dripleaf: SoundType.SMALL_DRIPLEAF,
        rooted_dirt: SoundType.ROOTED_DIRT,
        hanging_roots: SoundType.HANGING_ROOTS,
        azalea_leaves: SoundType.AZALEA_LEAVES,
        sculk_sensor: SoundType.SCULK_SENSOR,
        sculk_catalyst: SoundType.SCULK_CATALYST,
        sculk: SoundType.SCULK,
        sculk_vein: SoundType.SCULK_VEIN,
        sculk_shrieker: SoundType.SCULK_SHRIEKER,
        glow_lichen: SoundType.GLOW_LICHEN,
        deepslate: SoundType.DEEPSLATE,
        deepslate_bricks: SoundType.DEEPSLATE_BRICKS,
        deepslate_tiles: SoundType.DEEPSLATE_TILES,
        polished_deepslate: SoundType.POLISHED_DEEPSLATE,
        froglight: SoundType.FROGLIGHT,
        frogspawn: SoundType.FROGSPAWN,
        mangrove_roots: SoundType.MANGROVE_ROOTS,
        muddy_mangrove_roots: SoundType.MUDDY_MANGROVE_ROOTS,
        mud: SoundType.MUD,
        mud_bricks: SoundType.MUD_BRICKS,
        packed_mud: SoundType.PACKED_MUD,
        hanging_sign: SoundType.HANGING_SIGN,
        nether_wood_hanging_sign: SoundType.NETHER_WOOD_HANGING_SIGN,
        bamboo_wood_hanging_sign: SoundType.BAMBOO_WOOD_HANGING_SIGN,
        bamboo_wood: SoundType.BAMBOO_WOOD,
        nether_wood: SoundType.NETHER_WOOD,
        cherry_wood: SoundType.CHERRY_WOOD,
        cherry_sapling: SoundType.CHERRY_SAPLING,
        cherry_leaves: SoundType.CHERRY_LEAVES,
        cherry_wood_hanging_sign: SoundType.CHERRY_WOOD_HANGING_SIGN,
        chiseled_bookshelf: SoundType.CHISELED_BOOKSHELF,
        suspicious_sand: SoundType.SUSPICIOUS_SAND,
        decorated_pot: SoundType.DECORATED_POT
    ])

    static final Codec<SoundType> SOUND_TYPE_NAMED_CODEC = ofMapCodec(SOUND_TYPE_MAP, "sound_type")
}
