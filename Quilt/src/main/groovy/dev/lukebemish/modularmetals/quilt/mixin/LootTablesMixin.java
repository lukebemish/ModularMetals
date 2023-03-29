package dev.lukebemish.modularmetals.quilt.mixin;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import dev.lukebemish.modularmetals.util.LootPlanner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;

@Mixin(LootTables.class)
public class LootTablesMixin {
    @ModifyVariable(method = "method_20711", argsOnly = true, at = @At("HEAD"))
    private static JsonElement modularmetals$modifyLootTable(JsonElement jsonElement, ImmutableMap.Builder<ResourceLocation, LootTable> builder, ResourceLocation resourceLocation, JsonElement jsonElementAgain) {
        return LootPlanner.modify(resourceLocation, jsonElement);
    }
}
