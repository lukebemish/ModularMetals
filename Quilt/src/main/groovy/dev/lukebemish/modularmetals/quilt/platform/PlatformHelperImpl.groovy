package dev.lukebemish.modularmetals.quilt.platform

import com.google.auto.service.AutoService
import com.mojang.datafixers.util.Pair
import dev.lukebemish.modularmetals.data.MobEffectProvider
import dev.lukebemish.modularmetals.data.recipe.WorldgenRecipe
import dev.lukebemish.modularmetals.quilt.ModularMetalsQuilt
import dev.lukebemish.modularmetals.quilt.Queues
import dev.lukebemish.modularmetals.quilt.QuiltBiomes
import dev.lukebemish.modularmetals.services.IPlatformHelper
import groovy.transform.CompileStatic
import groovy.transform.Memoized
import io.github.lukebemish.groovyduvet.core.api.RemappingCustomizer
import net.fabricmc.api.EnvType
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.food.FoodProperties
import net.minecraft.world.item.ItemStack
import org.codehaus.groovy.control.CompilerConfiguration
import org.quiltmc.loader.api.QuiltLoader
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader
import org.quiltmc.qsl.worldgen.biome.api.BiomeModifications

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
    void customize(CompilerConfiguration compilerConfiguration) {
        compilerConfiguration.addCompilationCustomizers(new RemappingCustomizer())
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

    @Override
    void addFeatureToBiomes(ResourceLocation feature, WorldgenRecipe recipe) {
        BiomeModifications.addFeature(
            {context -> recipe.biomeFilter.matches(context, QuiltBiomes.BIOME_FINDER)},
            recipe.decoration,
            ResourceKey.create(Registries.PLACED_FEATURE, feature)
        )
    }
}
