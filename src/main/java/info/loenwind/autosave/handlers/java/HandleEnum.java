package info.loenwind.autosave.handlers.java;

import info.loenwind.autosave.Registry;
import info.loenwind.autosave.annotations.Store;
import info.loenwind.autosave.handlers.IHandler;

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;

public class HandleEnum implements IHandler<Enum<?>> {

  public HandleEnum() {
  }

  @Override
  public boolean canHandle(Class<?> clazz) {
    return Enum.class.isAssignableFrom(clazz);
  }

  @Override
  public boolean store(@Nonnull Registry registry, @Nonnull Set<Store.StoreFor> phase, @Nonnull NBTTagCompound nbt, @Nonnull String name,
      @Nonnull Enum<?> object)
      throws IllegalArgumentException, IllegalAccessException {
    nbt.setInteger(name, object.ordinal());
    return true;
  }

  @Override
  public Enum<?> read(@Nonnull Registry registry, @Nonnull Set<Store.StoreFor> phase, @Nonnull NBTTagCompound nbt, @Nonnull String name,
      @Nullable Enum<?> object) {
    if (nbt.hasKey(name) && object != null) {
      Enum<?>[] enumConstants = object.getClass().getEnumConstants();
      return enumConstants[MathHelper.clamp_int(nbt.getInteger(name), 0, enumConstants.length - 1)];
    } else {
      return object;
    }
  }

}
