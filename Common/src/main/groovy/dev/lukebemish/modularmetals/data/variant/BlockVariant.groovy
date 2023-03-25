package dev.lukebemish.modularmetals.data.variant

import com.mojang.serialization.Codec
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import io.github.groovymc.cgl.api.transform.codec.WithCodec
import io.github.groovymc.cgl.reg.RegistryObject
import dev.lukebemish.modularmetals.Constants
import dev.lukebemish.modularmetals.ModularMetalsCommon
import dev.lukebemish.modularmetals.client.variant.BlockClientVariantHandler
import dev.lukebemish.modularmetals.client.variant.ClientVariantHandler
import dev.lukebemish.modularmetals.data.Fillable
import dev.lukebemish.modularmetals.data.MapHolder
import dev.lukebemish.modularmetals.data.Metal
import dev.lukebemish.modularmetals.util.DataPlanner
import dev.lukebemish.modularmetals.util.MoreCodecs
import dev.lukebemish.modularmetals.util.TemplateUtils
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.StringRepresentable
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.Material
import net.minecraft.world.level.material.MaterialColor

@CodecSerializable(property = 'BLOCK_CODEC')
@TupleConstructor(includeSuperProperties = true, callSuper = true, includeFields = true)
@CompileStatic
class BlockVariant extends ItemVariant {
    private static final Map<String, Block> BLOCKS = new HashMap<>()

    protected BlockVariantTexturing texturing

    Optional<MapHolder> lootTable

    Optional<Fillable<BlockProperties>> blockProperties

    @TupleConstructor
    @CodecSerializable
    static class BlockProperties {
        @WithCodec({ MoreCodecs.MATERIAL_CODEC })
        final Material material = Material.METAL
        @WithCodec(value = { MoreCodecs.MATERIAL_COLOR_CODEC }, target = [0])
        final Optional<MaterialColor> materialColor = Optional.empty()
        final Collision collision = Collision.NORMAL
        @WithCodec({ MoreCodecs.SOUND_TYPE_NAMED_CODEC })
        final SoundType soundType = SoundType.METAL
        final float friction = 0.6f
        final float speedFactor = 1.0f
        final float jumpFactor = 1.0f
        final int lightLevel = 0
        final float destroyTime = 0.0f
        final Optional<Float> explosionResistance = Optional.empty()
        final boolean air = false
        final boolean requiresCorrectToolForDrops = false
        final boolean spawnParticlesOnBreak = true

        BlockBehaviour.Properties asProperties() {
            var props = BlockBehaviour.Properties.of(material, materialColor.orElseGet { -> material.color })
                .sound(soundType)
            if (collision == Collision.NO_COLLISION) {
                props = props.noCollission()
            } else if (collision == Collision.NO_OCCLUSION) {
                props = props.noOcclusion()
            }
            props = props.friction(friction)
                .speedFactor(speedFactor)
                .jumpFactor(jumpFactor)
                .lightLevel(s -> lightLevel)
                .strength(destroyTime)
            if (explosionResistance.isPresent()) {
                props = props.explosionResistance(explosionResistance.get())
            } else {
                props = props.explosionResistance(destroyTime)
            }
            if (air) {
                props = props.air()
            }
            if (requiresCorrectToolForDrops) {
                props = props.requiresCorrectToolForDrops()
            }
            if (!spawnParticlesOnBreak) {
                props = props.noParticlesOnBreak()
            }
            return props
        }

        static enum Collision implements StringRepresentable {
            NORMAL("normal"), NO_COLLISION("no_collision"), NO_OCCLUSION("no_occlusion")

            private final String name

            Collision(String name) {
                this.name = name
            }

            @Override
            String getSerializedName() {
                return this.name
            }
        }
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
        return BLOCK_CODEC
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
    RegistryObject<? extends Item> registerItem(String location, ResourceLocation variantRl, ResourceLocation metalRl, Metal metal, Map props) {
        ModularMetalsCommon.ITEMS.register(location, {->
            Block block = BLOCKS.get(location)
            return new BlockItem(block, makeProperties(props))
        })
    }

    @Override
    void register(Metal metal, ResourceLocation metalLocation, ResourceLocation variantLocation, Map<ResourceLocation, ResourceLocation> variantLocations) {
        var fullLocation = ModularMetalsCommon.assembleMetalVariantName(metalLocation, variantLocation)
        Map props = fillProperties(fullLocation, metalLocation, metal, variantLocations)
        String location = fullLocation.path
        registerBlock(location, variantLocation, metalLocation, metal, props)
        super.register(metal, metalLocation, variantLocation, variantLocations)

        getTags(metalLocation, props).each {
            ModularMetalsCommon.DATA_CACHE.tags().queue(new ResourceLocation(it.namespace, "blocks/${it.path}"), new ResourceLocation(Constants.MOD_ID, location))
        }

        if (this.lootTable.isPresent()) {
            var lootTableJson = TemplateUtils.init(lootTable.get(), ['location':fullLocation], metal, metalLocation, variantLocation, variantLocations, variantLocations.keySet().toList()).second
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

    RegistryObject<? extends Block> registerBlock(String location, ResourceLocation variantRl, ResourceLocation metalRl, Metal metal, Map props) {
        return ModularMetalsCommon.BLOCKS.register(location, {->
            BlockBehaviour.Properties properties = blockProperties.flatMap {
                it.apply(props).result()
            }.map {
                it.asProperties()
            }.orElseGet {
                new BlockProperties().asProperties()
            }
            Block block = new Block(properties)
            BLOCKS.put(location, block)
            return block
        })
    }

    String makeTranslationKey(String path) {
        return "block.${Constants.MOD_ID}.${path}"
    }
}
