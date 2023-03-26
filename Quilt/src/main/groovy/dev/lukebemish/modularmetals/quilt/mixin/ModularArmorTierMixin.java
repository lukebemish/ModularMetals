package dev.lukebemish.modularmetals.quilt.mixin;

import dev.lukebemish.modularmetals.data.tier.ModularArmorTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorMaterial;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ModularArmorTier.class, remap = false)
public abstract class ModularArmorTierMixin implements ArmorMaterial {

    @Override
    public @ClientOnly @NotNull ResourceLocation getTexture() {
        ResourceLocation name = new ResourceLocation(this.getName());
        return new ResourceLocation(name.getNamespace(), "textures/models/armor/" + name.getPath());
    }
}
