package crazypants.enderio.machine.spawner;

import java.util.List;

import javax.annotation.Nullable;

import com.enderio.core.client.gui.widget.GhostBackgroundItemSlot;
import com.enderio.core.client.gui.widget.GhostSlot;

import crazypants.enderio.init.ModObject;
import crazypants.enderio.machine.gui.AbstractMachineContainer;
import crazypants.enderio.network.GuiPacket;
import crazypants.enderio.network.IRemoteExec;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class ContainerPoweredSpawner extends AbstractMachineContainer<TilePoweredSpawner> implements IRemoteExec.IContainer {

  private Slot slotInput;
  private Slot slotOutput;

  public ContainerPoweredSpawner(InventoryPlayer playerInv, TilePoweredSpawner te) {
    super(playerInv, te);
  }

  @Override
  protected void addMachineSlots(InventoryPlayer playerInv) {
    slotInput = addSlotToContainer(new Slot(getInv(), 0, 54, 42) {
      @Override
      public boolean isItemValid(@Nullable ItemStack itemStack) {
        return getInv().isItemValidForSlot(0, itemStack);
      }
    });
    slotOutput = addSlotToContainer(new Slot(getInv(), 1, 105, 42) {
      @Override
      public boolean isItemValid(@Nullable ItemStack itemStack) {
        return false;
      }
    });
  }

  public void createGhostSlots(List<GhostSlot> slots) {
    final GhostBackgroundItemSlot ghostBackgroundItemSlot = new GhostBackgroundItemSlot(ModObject.itemSoulVial.getItem(), slotInput);
    ghostBackgroundItemSlot.y = 42;
    slots.add(ghostBackgroundItemSlot);
  }

  public void setSlotVisibility(boolean visible) {
    slotInput.yPos = visible ? 42 : -3000;
    slotOutput.yPos = visible ? 42 : -3000;
  }

  @Override
  public IMessage networkExec(int id, GuiPacket message) {
    switch (id) {
    case 0:
      //getInv().setSpawnMode(message.getBoolean(0)); TODO Implement
      break;
    default:
      break;
    }
    return null;
  }

}
