package crazypants.enderio.machine;

import java.util.EnumMap;

import javax.annotation.Nonnull;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import crazypants.enderio.render.EnumRenderPart;
import crazypants.enderio.render.IOMode.EnumIOMode;

@SideOnly(Side.CLIENT)
public final class RenderMappers {

  public static final MachineRenderMapper FRONT_MAPPER = new MachineRenderMapper(null);
  
  public static final MachineRenderMapper FRONT_MAPPER_NO_IO = new MachineRenderMapper(null) {

    @Override
    protected EnumMap<EnumFacing, EnumIOMode> renderIO(@Nonnull AbstractMachineEntity tileEntity, @Nonnull AbstractMachineBlock<?> block) {
      return null;
    }
    
  };
  
  public static final MachineRenderMapper BODY_MAPPER = new MachineRenderMapper(EnumRenderPart.BODY);
  
  public static final MachineRenderMapper SOUL_MAPPER = new MachineRenderMapper(EnumRenderPart.SOUL);
  
}
