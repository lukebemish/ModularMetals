package dev.lukebemish.modularmetals.data.tier

import com.google.common.base.Suppliers
import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import dev.lukebemish.modularmetals.Constants
import dev.lukebemish.modularmetals.data.RegistryProvider
import dev.lukebemish.modularmetals.util.MoreCodecs
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.CodecSerializable
import io.github.groovymc.cgl.api.transform.codec.WithCodec
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import net.minecraft.tags.TagKey
import net.minecraft.world.item.ArmorItem
import net.minecraft.world.item.ArmorMaterial
import net.minecraft.world.item.crafting.Ingredient

import java.util.function.Supplier

@CompileStatic
@CodecSerializable
@TupleConstructor(excludes = ['repairIngredientFinal', 'name'], includeFields = true)
class ModularArmorTier implements ArmorMaterial {
    static final Map<ArmorItem.Type, Integer> DEFAULT_DURABILITY = Map.of(
        ArmorItem.Type.HELMET, 11,
        ArmorItem.Type.CHESTPLATE, 16,
        ArmorItem.Type.LEGGINGS, 15,
        ArmorItem.Type.BOOTS, 13
    )
    static Map<ArmorItem.Type, Integer> getDurability(int durability) {
        return DEFAULT_DURABILITY.collectEntries { type, value -> [type, value * durability] }
    }

    @WithCodec({
        return Codec.either(
            Codec.unboundedMap(MoreCodecs.ARMOR_TYPE_CODEC, Codec.INT),
            Codec.INT
        ).<Map<ArmorItem.Type,Integer>>xmap({
            it.<Map<ArmorItem.Type,Integer>>map({
                it
            },{
                getDurability(it)
            })
        },{
            Either.<Map<ArmorItem.Type,Integer>,Integer>left(it)
        })
    })
    protected final Map<ArmorItem.Type, Integer> durability = getDurability(5)
    @WithCodec(value = { MoreCodecs.ARMOR_TYPE_CODEC }, target = [0])
    protected final Map<ArmorItem.Type, Integer> defense = Map.of(
        ArmorItem.Type.HELMET, 1,
        ArmorItem.Type.CHESTPLATE, 3,
        ArmorItem.Type.LEGGINGS, 2,
        ArmorItem.Type.BOOTS, 1
    )
    protected final Integer enchantment = 9
    protected final RegistryProvider equipSound = new RegistryProvider(new ResourceLocation("item.armor.equip_iron"))
    @WithCodec(value = { MoreCodecs.INGREDIENT_CODEC }, target = [0])
    protected Optional<Supplier<Ingredient>> repairIngredient
    protected Supplier<Ingredient> repairIngredientFinal
    protected String name
    protected final Float toughness = 0.0f
    protected final Float knockbackResistance = 0.0f

    ModularArmorTier bake(ResourceLocation location) {
        repairIngredientFinal = repairIngredient.orElse(Suppliers.memoize {->
            Ingredient.of(TagKey.create(Registries.ITEM, new ResourceLocation(Constants.MOD_ID,"ingots/${location.path}")))
        })
        name = location.toString()
        return this
    }

    @Override
    int getDurabilityForType(ArmorItem.Type type) {
        return durability.get(type)?:0
    }

    @Override
    int getDefenseForType(ArmorItem.Type type) {
        return defense.get(type)?:0
    }

    @Override
    int getEnchantmentValue() {
        return enchantment
    }

    @Override
    SoundEvent getEquipSound() {
        return this.@equipSound.get(BuiltInRegistries.SOUND_EVENT)
    }

    @Override
    Ingredient getRepairIngredient() {
        return repairIngredientFinal.get()
    }

    @Override
    String getName() {
        return this.@name
    }

    @Override
    float getToughness() {
        return this.@toughness
    }

    @Override
    float getKnockbackResistance() {
        return this.@knockbackResistance
    }
}
