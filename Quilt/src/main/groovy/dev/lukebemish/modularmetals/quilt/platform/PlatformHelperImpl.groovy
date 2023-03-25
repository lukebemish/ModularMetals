package dev.lukebemish.modularmetals.quilt.platform

import com.google.auto.service.AutoService
import com.mojang.datafixers.util.Pair
import dev.lukebemish.modularmetals.data.MobEffectProvider
import dev.lukebemish.modularmetals.quilt.Queues
import groovy.transform.CompileStatic
import groovy.transform.Memoized
import dev.lukebemish.modularmetals.quilt.ModularMetalsQuilt
import dev.lukebemish.modularmetals.services.IPlatformHelper
import net.fabricmc.api.EnvType
import net.minecraft.world.food.FoodProperties
import net.minecraft.world.item.ItemStack
import org.quiltmc.loader.api.QuiltLoader
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader

import java.nio.file.Path
import java.util.function.Supplier

@AutoService(IPlatformHelper)
@CompileStatic
class PlatformHelperImpl implements IPlatformHelper {

    @Override
    boolean isDevelopmentEnvironment() {
        return QuiltLoader.developmentEnvironment
    }

    @Override
    boolean isClient() {
        return MinecraftQuiltLoader.environmentType == EnvType.CLIENT
    }

    @Override
    Path getConfigFolder() {
        return QuiltLoader.configDir
    }

    @Override
    void addTabItem(Supplier<ItemStack> itemStackSupplier) {
        ModularMetalsQuilt.TAB_ITEMS.add(itemStackSupplier)
    }

    @Override
    Platform getPlatform() {
        return Platform.QUILT
    }

    @Override
    @Memoized
    Set<String> modList() {
        return QuiltLoader.getAllMods().collect {it.metadata().id()}.toSet()
    }

    @Override
    FoodProperties platformData(FoodProperties.Builder builder, List<MobEffectProvider> effects) {
        var built = builder.build()
        Queues.FOOD_QUEUE.add(Pair.of(built, effects))
        return built
    }
}
