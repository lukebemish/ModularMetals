package io.github.lukebemish.modularmetals.data.variant

import com.mojang.serialization.Codec
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import io.github.groovymc.cgl.api.transform.codec.WithCodec
import io.github.groovymc.cgl.reg.RegistryObject
import io.github.lukebemish.modularmetals.Constants
import io.github.lukebemish.modularmetals.ModularMetalsCommon
import io.github.lukebemish.modularmetals.client.variant.BlockClientVariantHandler
import io.github.lukebemish.modularmetals.client.variant.ClientVariantHandler
import io.github.lukebemish.modularmetals.data.MapHolder
import io.github.lukebemish.modularmetals.data.Metal
import io.github.lukebemish.modularmetals.util.DataPlanner
import io.github.lukebemish.modularmetals.util.MoreCodecs
import io.github.lukebemish.modularmetals.util.TemplateUtils
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.Material

@CodecSerializable
@TupleConstructor(includeSuperProperties = true, callSuper = true, includeFields = true)
class BlockVariant extends ItemVariant {
    private static final Map<String, Block> BLOCKS = new HashMap<>()

    protected BlockVariantTexturing texturing

    Optional<MapHolder> lootTable

    BlockProperties blockProperties = new BlockProperties(Optional.empty())

    @TupleConstructor
    @CodecSerializable
    static class BlockProperties {
        @WithCodec({ MoreCodecs.MATERIAL_CODEC })
        Optional<Material> material
    }

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

    @Override
    ClientVariantHandler getClientHandler() {
        return new BlockClientVariantHandler()
    }

    @TupleConstructor(includeSuperProperties = true, callSuper = true)
    @CodecSerializable
    static class BlockVariantTexturing extends ItemVariant.ItemVariantTexturing {
        final Optional<MapHolder> blockstate
    }

    @Override
    RegistryObject<? extends Item> registerItem(String location, ResourceLocation variantRl, ResourceLocation metalRl, Metal metal) {
        ModularMetalsCommon.ITEMS.register(location, {->
            Block block = BLOCKS.get(location)
            return new BlockItem(block, new Item.Properties())
        })
    }

    @Override
    void register(Metal metal, ResourceLocation metalLocation, ResourceLocation variantLocation, Map<ResourceLocation, ResourceLocation> variantLocations) {
        var fullLocation = ModularMetalsCommon.assembleMetalVariantName(metalLocation, variantLocation)
        String location = fullLocation.path
        registerBlock(location, variantLocation, metalLocation, metal)
        super.register(metal, metalLocation, variantLocation, variantLocations)

        getTags(metalLocation).each {
            ModularMetalsCommon.DATA_CACHE.tags().queue(new ResourceLocation(it.namespace, "blocks/${it.path}"), new ResourceLocation(Constants.MOD_ID, location))
        }

        if (this.lootTable.isPresent()) {
            var lootTableJson = TemplateUtils.init(lootTable.get(), metal, metalLocation, variantLocation, variantLocations, variantLocations.keySet().toList()).second
            DataPlanner.instance.blockLoot(fullLocation, lootTableJson)
        } else {
            var map = [
                "type": "minecraft:block",
                "pools": [[
                              "bonus_rolls": 0.0,
                              "conditions": [["condition": "minecraft:survives_explosion"]],
                              "entries": [["type": "minecraft:item", "name": fullLocation.toString()]],
                              "rolls": 1.0]]]
            var lootTableJson = Constants.GSON.toJsonTree(map)
            DataPlanner.instance.blockLoot(fullLocation, lootTableJson)
        }
    }

    RegistryObject<? extends Block> registerBlock(String location, ResourceLocation variantRl, ResourceLocation metalRl, Metal metal) {
        return ModularMetalsCommon.BLOCKS.register(location, {->
            Block block = new Block(BlockBehaviour.Properties.of(blockProperties.material.orElse(Material.METAL)))
            BLOCKS.put(location, block)
            return block
        })
    }

    String makeTranslationKey(String path) {
        return "block.${Constants.MOD_ID}.${path}"
    }
}
