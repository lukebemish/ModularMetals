package io.github.lukebemish.modularmetals.data.variant

import com.mojang.serialization.Codec
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.reg.RegistryObject
import io.github.lukebemish.groovyduvet.wrapper.minecraft.api.codec.CodecSerializable
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

@CompileStatic
@CodecSerializable
@TupleConstructor(includeSuperProperties = true, callSuper = true)
class BlockVariant extends ItemVariant {
    private static final Map<String, Block> BLOCKS = new HashMap<>()

    final BlockVariantTexturing texturing

    @Override
    Codec getCodec() {
        return $CODEC
    }

    @Override
    boolean isEnabledByDefault() {
        return defaultEnabled.orElse(true)
    }

    @TupleConstructor(includeSuperProperties = true, callSuper = true)
    @CodecSerializable
    static class BlockVariantTexturing extends ItemVariant.ItemVariantTexturing {
        final Optional<MapHolder> blockstate
    }

    @Override
    RegistryObject<? extends Item> registerItem(String location, Metal metal, ResourceLocation metalRl) {
        return ModularMetalsCommon.ITEMS.register(location, {->
            Block block = BLOCKS.get(location)
            return new BlockItem(block, new Item.Properties().tab(Services.PLATFORM.getBlockTab()))
        })
    }

    RegistryObject<? extends Block> registerBlock(String location, Metal metal, ResourceLocation metalRl) {
        return ModularMetalsCommon.BLOCKS.register(location, {->
            Block block = new Block(BlockBehaviour.Properties.of(Material.METAL, MaterialColor.COLOR_GRAY))
            BLOCKS.put(location, block)
            return block
        })
    }
}
