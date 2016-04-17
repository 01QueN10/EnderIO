package crazypants.enderio.machine.farm;

import java.util.Random;

import javax.annotation.Nonnull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import crazypants.enderio.GuiHandler;
import crazypants.enderio.ModObject;
import crazypants.enderio.machine.AbstractMachineBlock;
import crazypants.enderio.machine.IoMode;
import crazypants.enderio.network.PacketHandler;
import crazypants.enderio.paint.IPaintable;
import crazypants.enderio.render.IBlockStateWrapper;
import crazypants.enderio.render.IOMode;
import crazypants.enderio.render.IRenderMapper;
import crazypants.enderio.render.IRenderMapper.IItemRenderMapper;

public class BlockFarmStation extends AbstractMachineBlock<TileFarmStation> implements IPaintable.INonSolidBlockPaintableBlock, IPaintable.IWrenchHideablePaint {

  public static BlockFarmStation create() {
    PacketHandler.INSTANCE.registerMessage(PacketFarmAction.class, PacketFarmAction.class, PacketHandler.nextID(), Side.CLIENT);
    PacketHandler.INSTANCE.registerMessage(PacketUpdateNotification.class, PacketUpdateNotification.class, PacketHandler.nextID(), Side.CLIENT);
    PacketHandler.INSTANCE.registerMessage(PacketFarmLockedSlot.class, PacketFarmLockedSlot.class, PacketHandler.nextID(), Side.SERVER);
    BlockFarmStation result = new BlockFarmStation();
    result.init();
    return result;
  }

  protected BlockFarmStation() {
    super(ModObject.blockFarmStation, TileFarmStation.class);
  }

  @Override
  public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
    TileFarmStation te = getTileEntity(world, new BlockPos(x, y, z));
    if (te != null) {
      return new FarmStationContainer(player.inventory, te);
    }
    return null;
  }

  @Override
  public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
    TileFarmStation te = getTileEntity(world, new BlockPos(x, y, z));
    if (te != null) {
      return new GuiFarmStation(player.inventory, te);
    }
    return null;
  }

  @Override
  protected int getGuiId() {
    return GuiHandler.GUI_ID_FARM_STATATION;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public IOMode.EnumIOMode mapIOMode(IoMode mode, EnumFacing side) {
    if (side == EnumFacing.UP || side == EnumFacing.DOWN) {
      switch (mode) {
      case NONE:
        return IOMode.EnumIOMode.NONE;
      case PULL:
        return IOMode.EnumIOMode.PULL;
      case PUSH:
        return IOMode.EnumIOMode.PUSH;
      case PUSH_PULL:
        return IOMode.EnumIOMode.PUSHPULL;
      case DISABLED:
        return IOMode.EnumIOMode.DISABLED;
      }
    } else {
      switch (mode) {
      case NONE:
        return IOMode.EnumIOMode.NONE;
      case PULL:
        return IOMode.EnumIOMode.PULLSIDES;
      case PUSH:
        return IOMode.EnumIOMode.PUSHSIDES;
      case PUSH_PULL:
        return IOMode.EnumIOMode.PUSHPULLSIDES;
      case DISABLED:
        return IOMode.EnumIOMode.DISABLEDSIDES;
      }
    }
    throw new RuntimeException("Hey, leave our enums alone!");
  }

  @Override
  public boolean shouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
    return true;
  }

  @Override
  public boolean isOpaqueCube() {
    return false;
  }

  @Override
  public void randomDisplayTick(World world, BlockPos pos, IBlockState state, Random rand) {
  }

  @Override
  public String getUnlocalizedNameForTooltip(ItemStack stack) {
    return getUnlocalizedName();
  }

  @Override
  @SideOnly(Side.CLIENT)
  public IItemRenderMapper getItemRenderMapper() {
    return FarmingStationRenderMapper.instance;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public IRenderMapper.IBlockRenderMapper getBlockRenderMapper() {
    return FarmingStationRenderMapper.instance;
  }

  @Override
  protected void setBlockStateWrapperCache(@Nonnull IBlockStateWrapper blockStateWrapper, @Nonnull IBlockAccess world, @Nonnull BlockPos pos,
      @Nonnull TileFarmStation tileEntity) {
    blockStateWrapper.addCacheKey(tileEntity.isActive());
  }

}
