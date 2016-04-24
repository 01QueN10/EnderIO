package crazypants.enderio.machine.crafter;

import info.loenwind.autosave.annotations.Storable;
import info.loenwind.autosave.annotations.Store;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

@Storable
public class DummyCraftingGrid implements IInventory {

  @Store
  ItemStack[] inv = new ItemStack[10];

  public boolean hasValidRecipe() {
    return getOutput() != null;
  }

  public ItemStack getOutput() {
    return inv[9];
  }

  @Override
  public int getSizeInventory() {
    return inv.length;
  }

  @Override
  public ItemStack getStackInSlot(int var1) {
    if (var1 < 0 || var1 >= inv.length) {
      return null;
    }
    return inv[var1];
  }

  @Override
  public ItemStack decrStackSize(int fromSlot, int amount) {
    ItemStack item = inv[fromSlot];
    inv[fromSlot] = null;
    if (item == null) {
      return null;
    }
    item.stackSize = 0;
    return item;
  }

  @Override
  public void setInventorySlotContents(int i, ItemStack itemstack) {
    if (itemstack != null) {
      inv[i] = itemstack.copy();
      if (i < 9) {
        inv[i].stackSize = 0;
      }
    } else {
      inv[i] = null;
    }
  }

  @Override
  public void clear() {
    for (int i = 0; i < inv.length; i++) {
      inv[i] = null;
    }
  }

  @Override
  public ItemStack removeStackFromSlot(int index) {
    ItemStack res = getStackInSlot(index);
    setInventorySlotContents(index, null);
    return res;
  }

  @Override
  public String getName() {
    return "CraftingGrid";
  }

  @Override
  public boolean hasCustomName() {
    return false;
  }

  @Override
  public int getInventoryStackLimit() {
    return 0;
  }

  @Override
  public void markDirty() {
  }

  @Override
  public boolean isUseableByPlayer(EntityPlayer var1) {
    return true;
  }

  @Override
  public void openInventory(EntityPlayer e) {
  }

  @Override
  public void closeInventory(EntityPlayer e) {
  }

  @Override
  public boolean isItemValidForSlot(int var1, ItemStack var2) {
    return var1 < 9;
  }

  public void readFromNBT(NBTTagCompound nbtRoot) {
    NBTTagList itemList = (NBTTagList) nbtRoot.getTag("Items");
    if (itemList == null) {
      for (int i = 0; i < inv.length; i++) {
        inv[i] = null;
      }
      return;
    }
    for (int i = 0; i < itemList.tagCount(); i++) {
      NBTTagCompound itemStack = itemList.getCompoundTagAt(i);
      byte slot = itemStack.getByte("Slot");
      if (slot >= 0 && slot < inv.length) {
        inv[slot] = ItemStack.loadItemStackFromNBT(itemStack);
      }
    }
  }

  public void writeToNBT(NBTTagCompound nbtRoot) {
    NBTTagList itemList = new NBTTagList();
    for (int i = 0; i < inv.length; i++) {
      if (inv[i] != null) {
        NBTTagCompound itemStackNBT = new NBTTagCompound();
        itemStackNBT.setByte("Slot", (byte) i);
        inv[i].writeToNBT(itemStackNBT);
        itemList.appendTag(itemStackNBT);
      }
    }
    nbtRoot.setTag("Items", itemList);
  }

  @Override
  public IChatComponent getDisplayName() {
    return new ChatComponentText(getName());
  }

  @Override
  public int getField(int id) {
    return 0;
  }

  @Override
  public void setField(int id, int value) {
  }

  @Override
  public int getFieldCount() {
    return 0;
  }

}
