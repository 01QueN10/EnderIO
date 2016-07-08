package crazypants.enderio.render;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * Un-sided alternative to {@link net.minecraft.client.renderer.color.IBlockColor} to be used together with the {@link PaintTintHandler}
 *
 */
public interface ITintedBlock {

  int getBlockTint(IBlockState state, @Nullable IBlockAccess worldIn, @Nullable BlockPos pos, int tintIndex);

}
