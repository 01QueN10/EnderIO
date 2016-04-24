package info.loenwind.autosave.handlers.java;

import info.loenwind.autosave.Registry;
import info.loenwind.autosave.annotations.Store.StoreFor;
import info.loenwind.autosave.exceptions.NoHandlerFoundException;
import info.loenwind.autosave.handlers.IHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.nbt.NBTTagCompound;

public class HandleArrayList<E extends Object> implements IHandler<List<E>> {

  private final IHandler<E> elemHandler;

  protected HandleArrayList(IHandler<E> elemHandler) {
    this.elemHandler = elemHandler;
  }

  @Override
  public boolean canHandle(Class<?> clazz) {
    // This handler needs to be sub-classed and annotated to be used because the Generics on the List<E> will have been deleted when canHandle() would need them
    return false;
  }

  @Override
  public boolean store(@Nonnull Registry registry, @Nonnull Set<StoreFor> phase, @Nonnull NBTTagCompound nbt, @Nonnull String name, @Nonnull List<E> object)
      throws IllegalArgumentException, IllegalAccessException, InstantiationException, NoHandlerFoundException {
    NBTTagCompound tag = new NBTTagCompound();
    tag.setInteger("size", object.size());
    for (int i = 0; i < object.size(); i++) {
      E elem = object.get(i);
      if (elem != null) {
        elemHandler.store(registry, phase, tag, i + "", elem);
      }
    }
    nbt.setTag(name, tag);
    return true;
  }

  @Override
  public List<E> read(@Nonnull Registry registry, @Nonnull Set<StoreFor> phase, @Nonnull NBTTagCompound nbt, @Nonnull String name, @Nullable List<E> object)
      throws IllegalArgumentException, IllegalAccessException, InstantiationException, NoHandlerFoundException {
    if (nbt.hasKey(name)) {
      if (object == null) {
        object = new ArrayList<E>();
      } else {
        object.clear();
      }

      NBTTagCompound tag = nbt.getCompoundTag(name);
      int size = tag.getInteger("size");
      for (int i = 0; i < size; i++) {
        if (tag.hasKey(i + "")) {
          object.add(elemHandler.read(registry, phase, tag, i + "", null));
        } else {
          object.add(null);
        }
      }
    }
    return object;
  }

}
