package dev.lukebemish.modularmetals.quilt

import dev.lukebemish.modularmetals.data.filter.resource.ResourceFilterFinder
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.level.biome.Biome
import org.quiltmc.qsl.worldgen.biome.api.BiomeSelectionContext

class QuiltBiomes {
    private QuiltBiomes() {}

    static final ResourceFilterFinder<BiomeSelectionContext> BIOME_FINDER = new ResourceFilterFinder<BiomeSelectionContext>() {
        @Override
        boolean isTag(BiomeSelectionContext thing, ResourceLocation tag) {
            TagKey<Biome> key = TagKey.create(Registries.BIOME, tag)
            return thing.isIn(key)
        }

        @Override
        boolean isLocation(BiomeSelectionContext thing, ResourceLocation location) {
            return thing.biomeKey.location() == location
        }
    }
}
