package dev.lukebemish.modularmetals.forge

import com.mojang.serialization.Codec
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import io.github.groovymc.cgl.api.transform.codec.WithCodec
import dev.lukebemish.modularmetals.data.filter.resource.ResourceFilter
import dev.lukebemish.modularmetals.data.filter.resource.ResourceFilterFinder
import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.levelgen.GenerationStep.Decoration
import net.minecraft.world.level.levelgen.placement.PlacedFeature
import net.minecraftforge.common.world.BiomeModifier
import net.minecraftforge.common.world.ModifiableBiomeInfo

@TupleConstructor
@CodecSerializable
class FilterFeatureBiomeModifier implements BiomeModifier {
    final ResourceFilter filter
    @WithCodec({PlacedFeature.CODEC})
    final Holder<PlacedFeature> feature
    final Decoration decoration

    static final ResourceFilterFinder<Holder<Biome>> BIOME_FINDER = new ResourceFilterFinder<Holder<Biome>>() {
        @Override
        boolean isTag(Holder<Biome> thing, ResourceLocation tag) {
            TagKey<Biome> key = TagKey.create(Registries.BIOME, tag)
            return thing.is(key)
        }

        @Override
        boolean isLocation(Holder<Biome> thing, ResourceLocation location) {
            return thing.unwrapKey().orElse(null)?.location() == location
        }
    }

    @Override
    void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase == Phase.ADD && filter.matches(biome, BIOME_FINDER)) {
            builder.generationSettings.addFeature(decoration, feature)
        }
    }

    @Override
    Codec<? extends BiomeModifier> codec() {
        return $CODEC
    }
}
