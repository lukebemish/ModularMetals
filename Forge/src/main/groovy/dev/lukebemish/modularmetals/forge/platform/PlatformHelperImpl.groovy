package dev.lukebemish.modularmetals.forge.platform

import com.google.auto.service.AutoService
import com.google.common.base.Suppliers
import dev.lukebemish.modularmetals.data.MobEffectProvider
import groovy.transform.CompileStatic
import groovy.transform.Memoized
import dev.lukebemish.modularmetals.forge.ModularMetalsForge
import dev.lukebemish.modularmetals.services.IPlatformHelper
import net.minecraft.world.food.FoodProperties
import net.minecraft.world.item.ItemStack
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.loading.FMLLoader
import net.minecraftforge.fml.loading.FMLPaths

import java.nio.file.Path
import java.util.function.Supplier

@AutoService(IPlatformHelper)
@CompileStatic
class PlatformHelperImpl implements IPlatformHelper {

    @Override
    boolean isDevelopmentEnvironment() {
        return !FMLLoader.production
    }

    @Override
    boolean isClient() {
        return FMLLoader.dist == Dist.CLIENT
    }

    @Override
    Path getConfigFolder() {
        return FMLPaths.CONFIGDIR.get()
    }

    @Override
    void addTabItem(Supplier<ItemStack> itemStackSupplier) {
        ModularMetalsForge.TAB_ITEMS.add(itemStackSupplier)
    }

    @Override
    Platform getPlatform() {
        return Platform.FORGE
    }

    @Override
    @Memoized
    Set<String> modList() {
        return ModList.get().mods.collect {it.modId}.toSet()
    }

    @Override
    FoodProperties platformData(FoodProperties.Builder builder, List<MobEffectProvider> effects) {
        for (MobEffectProvider effect : effects) {
            builder = builder.effect(Suppliers.memoize {->effect.provide()}, effect.probability)
        }
        return builder.build()
    }
}
