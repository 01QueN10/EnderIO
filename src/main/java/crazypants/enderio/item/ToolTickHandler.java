package crazypants.enderio.item;

import crazypants.enderio.api.tool.IConduitControl;
import crazypants.enderio.conduit.ConduitDisplayMode;
import crazypants.enderio.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ToolTickHandler {

  @SubscribeEvent
  public void onMouseEvent(MouseEvent event) {
    EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
    if(event.getDwheel() != 0 && player != null && player.isSneaking()) {
      ItemStack stack = player.getHeldItemMainhand();
      if (stack != null) {
        Item item = stack.getItem();
        if (item instanceof IConduitControl) {
          changeDisplayMode(stack, player, event.getDwheel());
          event.setCanceled(true);
        } else if (item instanceof ItemConduitProbe) {
          changeConduitProbeMode(stack);
          event.setCanceled(true);
        }
      }
    }
  }

  private void changeDisplayMode(ItemStack stack, EntityPlayer player, int dWheel) {
    if (!((IConduitControl)stack.getItem()).showOverlay(stack, player)) {
      return;
    }
    ConduitDisplayMode mode = ConduitDisplayMode.getDisplayMode(stack);
    if(mode == null) {
      mode = ConduitDisplayMode.ALL;
    }
    mode = dWheel < 0 ? mode.next() : mode.previous();
    ConduitDisplayMode.setDisplayMode(stack, mode);
    PacketHandler.INSTANCE.sendToServer(new YetaWrenchPacketProcessor(player.inventory.currentItem, mode));
  }

  private void changeConduitProbeMode(ItemStack stack) {
    int newMeta = stack.getItemDamage() == 0 ? 1 : 0;
    stack.setItemDamage(newMeta);
    PacketHandler.INSTANCE.sendToServer(new PacketConduitProbeMode());
  }

}
