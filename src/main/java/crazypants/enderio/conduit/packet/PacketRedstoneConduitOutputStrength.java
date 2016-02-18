package crazypants.enderio.conduit.packet;

import crazypants.enderio.conduit.redstone.IInsulatedRedstoneConduit;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketRedstoneConduitOutputStrength extends AbstractConduitPacket<IInsulatedRedstoneConduit> implements
    IMessageHandler<PacketRedstoneConduitOutputStrength, IMessage> {

  private EnumFacing dir;
  private boolean isStrong;

  public PacketRedstoneConduitOutputStrength() {
  }

  public PacketRedstoneConduitOutputStrength(IInsulatedRedstoneConduit con, EnumFacing dir) {
    super(con.getBundle().getEntity(), ConTypeEnum.REDSTONE);
    this.dir = dir;
    isStrong = con.isOutputStrong(dir);
  }

  @Override
  public void toBytes(ByteBuf buf) {
    super.toBytes(buf);
    if(dir == null) {
      buf.writeShort(-1);
    }else {
      buf.writeShort(dir.ordinal());
    }
    buf.writeBoolean(isStrong);
  }

  @Override
  public void fromBytes(ByteBuf buf) {
    super.fromBytes(buf);
    short ord = buf.readShort();
    if(ord < 0) {
      dir = null;
    } else {
      dir = EnumFacing.values()[ord];
    }
    isStrong = buf.readBoolean();
  }

  @Override
  public IMessage onMessage(PacketRedstoneConduitOutputStrength message, MessageContext ctx) {
    IInsulatedRedstoneConduit tile = message.getTileCasted(ctx);
    if(tile != null) {
      tile.setOutputStrength(message.dir, message.isStrong);
      //message.getWorld(ctx).markBlockForUpdate(message.x, message.y, message.z);
    }
    return null;
  }
}
