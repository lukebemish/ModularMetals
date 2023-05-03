package dev.lukebemish.modularmetals.forge.mixin;

import dev.lukebemish.modularmetals.objects.MMBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.extensions.IForgeBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(MMBlock.class)
public abstract class ModularMetalsBlockMixin extends Block implements IForgeBlock {
    public ModularMetalsBlockMixin(Properties props) {
        super(props);
    }

    /**
     * @author Luke Bemish
     * @reason Platform-specific mixin to my own class. If this fails, something's gone funky.
     */
    @Overwrite(remap = false)
    private void internalXpCreation(boolean dropExperience, ServerLevel level, BlockPos pos, ItemStack stack) {
        // pass
    }

    @Override
    public int getExpDrop(BlockState state, LevelReader level, RandomSource randomSource, BlockPos pos, int fortuneLevel, int silkTouchLevel) {
        //noinspection DataFlowIssue
        var experience = ((MMBlock) (Object) this).getExperience();
        return silkTouchLevel == 0 ? experience.sample(randomSource) : 0;
    }
}
