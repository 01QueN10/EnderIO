package crazypants.enderio.machine.painter.recipe;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import crazypants.enderio.machine.painter.blocks.BlockPaintedPressurePlate;

public class PressurePlatePainterTemplate extends BasicPainterTemplate<BlockPaintedPressurePlate> {

  private final int meta;

  public PressurePlatePainterTemplate(BlockPaintedPressurePlate resultBlock, int meta, Block... validTargetBlocks) {
    super(resultBlock, validTargetBlocks);
    this.meta = meta;
  }

  @Override
  protected ItemStack mkItemStack(ItemStack target, Block targetBlock) {
    if (targetBlock == resultBlock) {
      return new ItemStack(targetBlock, 1, meta);
    } else {
      return super.mkItemStack(target, targetBlock);
    }
  }

  @Override
  public boolean isValidTarget(ItemStack target) {
    return target != null && Block.getBlockFromItem(target.getItem()) != resultBlock && super.isValidTarget(target);
  }

}
