package crazypants.enderio.machine.soul;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import crazypants.enderio.machine.AbstractMachineBlock;
import crazypants.enderio.machine.AbstractMachineEntity;
import crazypants.enderio.machine.MachineRenderMapper;
import crazypants.enderio.render.EnumRenderMode;
import crazypants.enderio.render.EnumRenderPart;
import crazypants.enderio.render.dummy.BlockMachineBase;

public class SoulBinderBlockRenderMapper extends MachineRenderMapper {

  public static final SoulBinderBlockRenderMapper instance = new SoulBinderBlockRenderMapper();

  private SoulBinderBlockRenderMapper() {
    super(EnumRenderPart.SOUL_FRAME);
  }

  @Override
  @SideOnly(Side.CLIENT)
  protected List<IBlockState> render(IBlockState state, IBlockAccess world, BlockPos pos, EnumWorldBlockLayer blockLayer, AbstractMachineEntity tileEntity,
      AbstractMachineBlock<?> block) {
    List<IBlockState> states = new ArrayList<IBlockState>();

    EnumFacing facing = tileEntity.getFacing();
    boolean active = ((TileSoulBinder) tileEntity).isWorking();

    states.add(BlockMachineBase.block.getDefaultState().withProperty(EnumRenderPart.SUB, body.rotate(facing)));

    if (!active) {
      states.add(state.withProperty(EnumRenderMode.RENDER, EnumRenderMode.FRONT.rotate(facing)));
    }

    return states;
  }

}
