package crazypants.enderio.machine.obelisk.spawn;

import java.util.ArrayList;
import java.util.List;

import crazypants.enderio.EnderIO;
import crazypants.enderio.ModObject;
import crazypants.enderio.capacitor.ICapacitorKey;
import crazypants.enderio.machine.SlotDefinition;
import crazypants.enderio.machine.obelisk.AbstractRangedTileEntity;
import crazypants.util.CapturedMob;
import info.loenwind.autosave.annotations.Storable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

@Storable
public abstract class AbstractMobObelisk extends AbstractRangedTileEntity {

  public static enum SpawnObeliskAction {
    ATTRACT("blockAttractor.action"),
    AVERT("blockSpawnGuard.action"),
    RELOCATE("blockSpawnRelocator.action"),
  
    ;
  
    private final String langKey;
  
    private SpawnObeliskAction(String langKey) {
      this.langKey = langKey;
    }
  
    public String getActionString() {
      return EnderIO.lang.localize(langKey);
    }
  }

  public AbstractMobObelisk(SlotDefinition slotDefinition, ICapacitorKey maxEnergyRecieved, ICapacitorKey maxEnergyStored, ICapacitorKey maxEnergyUsed) {
    super(slotDefinition, maxEnergyRecieved, maxEnergyStored, maxEnergyUsed);
  }

  @Override
  protected boolean isMachineItemValidForSlot(int i, ItemStack itemstack) {
    if(!slotDefinition.isInputSlot(i)) {
      return false;
    }
    return CapturedMob.containsSoul(itemstack);
  }

  @Override
  public boolean isActive() {
    return hasPower();
  }

  protected double usePower() {
    return usePower(getPowerUsePerTick());
  }

  protected int usePower(int wantToUse) {
    int used = Math.min(getEnergyStored(), wantToUse);
    setEnergyStored(Math.max(0, getEnergyStored() - used));
    return used;
  }

  protected boolean isMobInRange(EntityLivingBase mob) {
    if (mob == null || getBounds() == null) {
      return false;
    }    
    return getBounds().isVecInside(new Vec3d(mob.posX, mob.posY, mob.posZ));
  }

  protected boolean isMobInFilter(EntityLivingBase entity) {
    for (int i = slotDefinition.minInputSlot; i <= slotDefinition.maxInputSlot; i++) {
      CapturedMob mob = CapturedMob.create(inventory[i]);
      if (mob != null && mob.isSameType(entity)) {
        return true;
      }
    }
    return false;
  }

  public List<CapturedMob> getMobsInFilter() {
    List<CapturedMob> result = new ArrayList<CapturedMob>();
    for (int i = slotDefinition.minInputSlot; i <= slotDefinition.maxInputSlot; i++) {
      CapturedMob mob = CapturedMob.create(inventory[i]);
      if (mob != null) {
        result.add(mob);
      }
    }
    return result;
  }

  public AbstractMobObelisk(SlotDefinition slotDefinition, ModObject modObject) {
    super(slotDefinition, modObject);
  }

  public abstract SpawnObeliskAction getSpawnObeliskAction();

}