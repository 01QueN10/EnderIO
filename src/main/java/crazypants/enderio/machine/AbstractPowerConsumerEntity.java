package crazypants.enderio.machine;

import net.minecraft.util.EnumFacing;
import crazypants.enderio.ModObject;
import crazypants.enderio.capacitor.ICapacitorKey;
import crazypants.enderio.power.IInternalPowerReceiver;
import crazypants.enderio.power.PowerHandlerUtil;

public abstract class AbstractPowerConsumerEntity extends AbstractPoweredMachineEntity implements IInternalPowerReceiver {

  @Deprecated
  protected AbstractPowerConsumerEntity(SlotDefinition slotDefinition) {
    super(slotDefinition);
  }

  protected AbstractPowerConsumerEntity(SlotDefinition slotDefinition, ICapacitorKey maxEnergyRecieved, ICapacitorKey maxEnergyStored,
      ICapacitorKey maxEnergyUsed) {
    super(slotDefinition, maxEnergyRecieved, maxEnergyStored, maxEnergyUsed);
  }

  protected AbstractPowerConsumerEntity(SlotDefinition slotDefinition, ModObject modObject) {
    super(slotDefinition, modObject);
  }

  @Override
  public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
    if(isSideDisabled(from)) {
      return 0;
    }
    return PowerHandlerUtil.recieveInternal(this, maxReceive, from, simulate);
  }

  @Override
  public int getEnergyStored(EnumFacing from) {
    return getEnergyStored();
  }

  @Override
  public int getMaxEnergyStored(EnumFacing from) {
    return getMaxEnergyStored();
  }

}
