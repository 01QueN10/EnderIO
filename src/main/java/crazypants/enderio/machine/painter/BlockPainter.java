package crazypants.enderio.machine.painter;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import crazypants.enderio.GuiHandler;
import crazypants.enderio.ModObject;
import crazypants.enderio.machine.AbstractMachineBlock;
import crazypants.enderio.machine.MachineRecipeRegistry;
import crazypants.enderio.machine.painter.recipe.EveryPaintableRecipe;
import crazypants.enderio.paint.IPaintable;
import crazypants.enderio.render.IBlockStateWrapper;

public class BlockPainter extends AbstractMachineBlock<TileEntityPainter> implements IPaintable.ISolidBlockPaintableBlock, IPaintable.IWrenchHideablePaint {

  public static BlockPainter create() {
    BlockPainter ppainter = new BlockPainter();
    ppainter.init();
    return ppainter;
  }

  private BlockPainter() {
    super(ModObject.blockPainter, TileEntityPainter.class);
  }

  @SuppressWarnings("rawtypes")
  @Override
  protected void init() {
    super.init();
    MachineRecipeRegistry.instance.enableRecipeSorting(ModObject.blockPainter.getUnlocalisedName());
    MachineRecipeRegistry.instance.registerRecipe(ModObject.blockPainter.getUnlocalisedName(), new EveryPaintableRecipe());
  }

  @Override
  public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
    TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
    if(te instanceof TileEntityPainter) {
      return new ContainerPainter(player.inventory, (TileEntityPainter) te);
    }
    return null;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
    TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
    if (te instanceof TileEntityPainter) {
      return new GuiPainter(player.inventory, (TileEntityPainter) te);
    }
    return null;
  }

  @Override
  protected int getGuiId() {
    return GuiHandler.GUI_ID_PAINTER;
  }

  @Override
  protected void setBlockStateWrapperCache(@Nonnull IBlockStateWrapper blockStateWrapper, @Nonnull IBlockAccess world, @Nonnull BlockPos pos,
      @Nonnull TileEntityPainter tileEntity) {
    blockStateWrapper.addCacheKey(tileEntity.getFacing()).addCacheKey(tileEntity.isActive());
  }

}
