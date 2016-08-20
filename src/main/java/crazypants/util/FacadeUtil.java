package crazypants.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.Optional.Method;
import team.chisel.api.IFacade;

public class FacadeUtil {

  public static final FacadeUtil instance = new InnerFacadeUtil();

  private FacadeUtil() {
  }

  private static class InnerFacadeUtil extends FacadeUtil {

    @Override
    @Method(modid = "ChiselAPI")
    public boolean isFacaded(IBlockState state) {
      return state != null && state.getBlock() instanceof IFacade;
    }

    @Override
    @Method(modid = "ChiselAPI")
    public IBlockState getFacade(IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nullable EnumFacing side) {
      return isFacaded(state) ? ((IFacade) state.getBlock()).getFacade(world, pos, side) : null;
    }

  }

  public boolean isFacaded(IBlockState state) {
    return false;
  }

  public IBlockState getFacade(IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nullable EnumFacing side) {
    return null;
  }

}
