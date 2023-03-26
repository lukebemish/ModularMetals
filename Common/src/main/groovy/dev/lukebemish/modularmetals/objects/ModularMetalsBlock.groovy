package dev.lukebemish.modularmetals.objects

import groovy.transform.CompileStatic
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.valueproviders.IntProvider
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState

@CompileStatic
class ModularMetalsBlock extends Block {
    final IntProvider experience

    ModularMetalsBlock(Properties properties, IntProvider experience = null) {
        super(properties)
        this.experience = experience
    }

    @SuppressWarnings(['deprecation', 'GrDeprecatedAPIUsage'])
    @Override
    void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack stack, boolean dropExperience) {
        super.spawnAfterBreak(state, level, pos, stack, dropExperience)
        internalXpCreation(dropExperience, level, pos, stack)
    }

    private void internalXpCreation(boolean dropExperience, ServerLevel level, BlockPos pos, ItemStack stack) {
        if (experience !== null && dropExperience) {
            this.tryDropExperience(level, pos, stack, experience)
        }
    }
}
