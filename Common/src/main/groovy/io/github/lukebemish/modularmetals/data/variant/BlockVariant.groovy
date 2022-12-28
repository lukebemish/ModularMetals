package io.github.lukebemish.modularmetals.data.variant

import com.mojang.serialization.Codec
import dev.lukebemish.dynamicassetgenerator.api.DataResourceCache
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import io.github.lukebemish.modularmetals.Constants
import io.github.lukebemish.modularmetals.ModularMetalsCommon
import io.github.lukebemish.modularmetals.data.MapHolder
import io.github.lukebemish.modularmetals.data.Metal
import io.github.lukebemish.modularmetals.services.Services
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.Material
import net.minecraft.world.level.material.MaterialColor

@CodecSerializable
@TupleConstructor(includeSuperProperties = true, callSuper = true, includeFields = true)
class BlockVariant extends ItemVariant {
    private static final Map<String, Block> BLOCKS = new HashMap<>()

    protected BlockVariantTexturing texturing

    @Override
    ItemVariantTexturing getTexturing() {
        return this.@texturing
    }

    BlockVariantTexturing getBlockTexturing() {
        return this.@texturing
    }

    @Override
    Codec getCodec() {
        return $CODEC
    }

    @TupleConstructor(includeSuperProperties = true, callSuper = true)
    @CodecSerializable
    static class BlockVariantTexturing extends ItemVariant.ItemVariantTexturing {
        final Optional<MapHolder> blockstate
    }

    @Override
    void registerItem(String location, ResourceLocation variantRl, ResourceLocation metalRl, Metal metal) {
        ModularMetalsCommon.ITEMS.register(location, {->
            Block block = BLOCKS.get(location)
            return new BlockItem(block, new Item.Properties())
        })
    }

    @Override
    void register(Metal metal, ResourceLocation metalLocation, ResourceLocation variantLocation) {
        String location = ModularMetalsCommon.assembleMetalVariantName(metalLocation, variantLocation).path
        registerBlock(location, variantLocation, metalLocation, metal)
        super.register(metal, metalLocation, variantLocation)
    }

    void registerBlock(String location, ResourceLocation variantRl, ResourceLocation metalRl, Metal metal) {
        getItemTags(metalRl).each {
            ModularMetalsCommon.DATA_CACHE.tags().queue(new ResourceLocation(it.namespace, "blocks/${it.path}"), new ResourceLocation(Constants.MOD_ID, location))
        }
        ModularMetalsCommon.BLOCKS.register(location, {->
            Block block = new Block(BlockBehaviour.Properties.of(Material.METAL, MaterialColor.COLOR_GRAY))
            BLOCKS.put(location, block)
            return block
        })
    }
}
