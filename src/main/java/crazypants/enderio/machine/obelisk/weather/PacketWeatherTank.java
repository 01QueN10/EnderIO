package crazypants.enderio.machine.obelisk.weather;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import com.enderio.core.common.network.MessageTileEntity;

import crazypants.enderio.EnderIO;

public class PacketWeatherTank extends MessageTileEntity<TileWeatherObelisk> implements IMessageHandler<PacketWeatherTank, IMessage> {

  private NBTTagCompound tag;

  public PacketWeatherTank() {
  }

  public PacketWeatherTank(TileWeatherObelisk tile) {
    super(tile);
    tag = tile.getInputTank().writeToNBT(new NBTTagCompound());
  }

  @Override
  public void toBytes(ByteBuf buf) {
    super.toBytes(buf);
    ByteBufUtils.writeTag(buf, tag);
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    super.fromBytes(buf);
    tag = ByteBufUtils.readTag(buf);
  }

  @Override
  public IMessage onMessage(PacketWeatherTank message, MessageContext ctx) {
    EntityPlayer player = EnderIO.proxy.getClientPlayer();
    TileWeatherObelisk tile = message.getTileEntity(player.worldObj);
    if (tile != null) {
      tile.getInputTank().readFromNBT(message.tag);
    }
    return null;
  }
}